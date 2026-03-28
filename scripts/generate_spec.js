#!/usr/bin/env node
/**
 * generate_spec.js — Parse vendor/api.ts + vendor/common-types.ts and emit
 * an OpenAPI 3.1.0 YAML spec to stdout.
 *
 * No TypeScript compiler required — uses regex/string parsing.
 */

'use strict';

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const API_TS = fs.readFileSync(path.join(ROOT, 'vendor/api.ts'), 'utf8');
const COMMON_TS = fs.readFileSync(path.join(ROOT, 'vendor/common-types.ts'), 'utf8');
const CHANGELOG = fs.readFileSync(path.join(ROOT, 'vendor/API-CHANGELOG.md'), 'utf8');

// ---------------------------------------------------------------------------
// 1. Extract version from changelog
// ---------------------------------------------------------------------------
const versionMatch = CHANGELOG.match(/^## V(\d+\.\d+\.\d+)/m);
const API_VERSION = versionMatch ? versionMatch[1] : '0.0.0';

// ---------------------------------------------------------------------------
// 2. Parse TypeScript interfaces
// ---------------------------------------------------------------------------

/** Strip single-line and block comments (but preserve string literals). */
function stripComments(src) {
  // Remove block comments
  let out = src.replace(/\/\*[\s\S]*?\*\//g, '');
  // Remove single-line comments
  out = out.replace(/\/\/.*$/gm, '');
  return out;
}

/**
 * Extract JSDoc description for a field from the raw (un-stripped) source.
 * Returns the comment text immediately before the field declaration at the given position.
 */
function extractFieldComment(rawSrc, fieldName, searchStart, searchEnd) {
  const region = rawSrc.slice(searchStart, searchEnd);
  // Find the field and look for JSDoc before it
  const fieldPattern = new RegExp(`\\/\\*\\*([\\s\\S]*?)\\*\\/\\s*(?:\\n\\s*)*${fieldName}\\??\\s*:`);
  const m = region.match(fieldPattern);
  if (!m) return null;
  // Clean up the JSDoc
  return m[1]
    .split('\n')
    .map(l => l.replace(/^\s*\*\s?/, '').trim())
    .filter(l => l && !l.startsWith('@'))
    .join(' ')
    .trim();
}

/**
 * Parse all interface/type declarations from TS source.
 * Returns { name: { extends: string|null, fields: [{name, type, optional, description}], isType, typeValue } }
 */
function parseDeclarations(rawSrc) {
  const src = stripComments(rawSrc);
  const decls = {};

  // Parse interfaces
  const ifaceRe = /export\s+interface\s+(\w+)(?:\s+extends\s+([\w\s,]+))?\s*\{/g;
  let m;
  while ((m = ifaceRe.exec(src)) !== null) {
    const name = m[1];
    const extendsClause = m[2] ? m[2].split(',').map(s => s.trim()).filter(Boolean) : [];
    const bodyStart = m.index + m[0].length;
    const body = extractBracedBlock(src, bodyStart);
    const fields = parseFields(body, rawSrc, m.index, m.index + m[0].length + body.length);
    decls[name] = { extends: extendsClause, fields, isType: false };
  }

  // Parse type aliases that are unions of interfaces (discriminated unions)
  const typeRe = /export\s+type\s+(\w+)\s*=\s*([^;]+);/g;
  while ((m = typeRe.exec(src)) !== null) {
    const name = m[1];
    const value = m[2].trim();
    decls[name] = { extends: [], fields: [], isType: true, typeValue: value };
  }

  return decls;
}

/** Extract the body of a brace-delimited block starting right after the opening brace. */
function extractBracedBlock(src, start) {
  let depth = 1;
  let i = start;
  while (i < src.length && depth > 0) {
    if (src[i] === '{') depth++;
    else if (src[i] === '}') depth--;
    i++;
  }
  return src.slice(start, i - 1);
}

/** Parse fields from an interface body string. */
function parseFields(body, rawSrc, rawStart, rawBodyStart) {
  const fields = [];
  // Match top-level fields: name?: type;
  // Also handle index signatures: [key: type]: type;
  const lines = body.split('\n');
  let i = 0;
  while (i < lines.length) {
    const line = lines[i].trim();

    // Index signature: [key: string]: valueType
    const indexMatch = line.match(/^\[(\w+)\s*:\s*(\w+)\]\s*:\s*(.+?)\s*;?\s*$/);
    if (indexMatch) {
      fields.push({
        name: '__indexSignature',
        keyType: indexMatch[2],
        type: indexMatch[3].replace(/;$/, '').trim(),
        optional: false,
        isIndex: true,
      });
      i++;
      continue;
    }

    // Regular field: name?: type;
    // Handle multi-line types by collecting until we find a semicolon
    const fieldStart = line.match(/^(\w+)(\??)\s*:\s*/);
    if (fieldStart) {
      const fieldName = fieldStart[1];
      const optional = fieldStart[2] === '?';
      let typeStr = line.slice(fieldStart[0].length);

      // If type contains an opening brace, collect the inline object
      if (typeStr.includes('{') && !typeStr.includes('}')) {
        i++;
        while (i < lines.length) {
          typeStr += ' ' + lines[i].trim();
          if (lines[i].includes('}')) {
            // Check if the brace closes the inline object
            const opens = (typeStr.match(/\{/g) || []).length;
            const closes = (typeStr.match(/\}/g) || []).length;
            if (closes >= opens) break;
          }
          i++;
        }
      }

      // If type has unbalanced parens (multi-line union in parens), collect more
      if (typeStr.includes('(') && !typeStr.includes(')')) {
        i++;
        while (i < lines.length) {
          typeStr += ' ' + lines[i].trim();
          if (typeStr.includes(')')) break;
          i++;
        }
      }

      typeStr = typeStr.replace(/;\s*$/, '').trim();
      const desc = extractFieldComment(rawSrc, fieldName, rawStart, rawBodyStart + body_offset(body, fieldName));

      fields.push({ name: fieldName, type: typeStr, optional, description: desc });
    }
    i++;
  }
  return fields;
}

function body_offset(body, fieldName) {
  const idx = body.indexOf(fieldName);
  return idx >= 0 ? idx : body.length;
}

// ---------------------------------------------------------------------------
// 3. Resolve inheritance and build schemas
// ---------------------------------------------------------------------------

const commonDecls = parseDeclarations(COMMON_TS);
const apiDecls = parseDeclarations(API_TS);
const allDecls = { ...commonDecls, ...apiDecls };

/** Resolve all fields for a declaration, including inherited ones. */
function resolveFields(name, seen = new Set()) {
  if (seen.has(name)) return [];
  seen.add(name);
  const decl = allDecls[name];
  if (!decl) return [];
  let fields = [];
  for (const parent of (decl.extends || [])) {
    fields = fields.concat(resolveFields(parent, seen));
  }
  fields = fields.concat(decl.fields);
  return fields;
}

/** Convert a TS type string to an OpenAPI schema object. */
function tsTypeToSchema(typeStr) {
  typeStr = typeStr.trim();

  // Remove trailing semicolons
  typeStr = typeStr.replace(/;$/, '').trim();

  // Parenthesized union: ( A | B | C )[]
  const parenUnionArr = typeStr.match(/^\(([^)]+)\)\[\]$/);
  if (parenUnionArr) {
    const inner = parenUnionArr[1];
    return { type: 'array', items: tsTypeToSchema(inner) };
  }

  // Array type: Type[]
  const arrMatch = typeStr.match(/^(.+)\[\]$/);
  if (arrMatch) {
    const inner = arrMatch[1].trim();
    // Handle union arrays like ('json' | 'usfm')[]
    if (inner.startsWith('(') && inner.endsWith(')')) {
      return { type: 'array', items: tsTypeToSchema(inner.slice(1, -1)) };
    }
    return { type: 'array', items: tsTypeToSchema(inner) };
  }

  // String literal union: 'a' | 'b' | 'c'
  if (/^'[^']*'(\s*\|\s*'[^']*')*$/.test(typeStr)) {
    const values = typeStr.match(/'([^']*)'/g).map(s => s.slice(1, -1));
    return { type: 'string', enum: values };
  }

  // Single string literal: 'value'
  if (/^'([^']*)'$/.test(typeStr)) {
    const val = typeStr.slice(1, -1);
    return { type: 'string', const: val };
  }

  // Union with null: Type | null
  const nullUnion = typeStr.match(/^(.+?)\s*\|\s*null$/);
  if (nullUnion) {
    const inner = tsTypeToSchema(nullUnion[1].trim());
    if (inner.type) {
      return { type: [inner.type, 'null'], ...(inner.enum ? { enum: inner.enum } : {}), ...(inner.const !== undefined ? { const: inner.const } : {}) };
    }
    if (inner.$ref) {
      return { oneOf: [inner, { type: 'null' }] };
    }
    return inner;
  }

  // null | Type
  const nullFirst = typeStr.match(/^null\s*\|\s*(.+)$/);
  if (nullFirst) {
    return tsTypeToSchema(nullFirst[1].trim() + ' | null');
  }

  // Union of types (not string literals, not null): A | B | C
  if (typeStr.includes('|') && !typeStr.startsWith('(')) {
    const parts = typeStr.split('|').map(s => s.trim());
    // Check if it's all string literals
    if (parts.every(p => /^'[^']*'$/.test(p))) {
      return { type: 'string', enum: parts.map(p => p.slice(1, -1)) };
    }
    // Mixed union - use oneOf
    const schemas = parts.filter(p => p !== 'null').map(p => tsTypeToSchema(p));
    if (parts.includes('null')) {
      return { oneOf: [...schemas, { type: 'null' }] };
    }
    return { oneOf: schemas };
  }

  // Inline object: { key: type; ... }
  if (typeStr.startsWith('{') && typeStr.endsWith('}')) {
    const inner = typeStr.slice(1, -1).trim();
    const props = {};
    const required = [];
    const fieldRe = /(\w+)(\??):\s*([^;]+)/g;
    let fm;
    while ((fm = fieldRe.exec(inner)) !== null) {
      props[fm[1]] = tsTypeToSchema(fm[3].trim());
      if (fm[2] !== '?') required.push(fm[1]);
    }
    const schema = { type: 'object', properties: props };
    if (required.length > 0) schema.required = required;
    return schema;
  }

  // Primitive types
  if (typeStr === 'string') return { type: 'string' };
  if (typeStr === 'number') return { type: 'integer' };
  if (typeStr === 'boolean') return { type: 'boolean' };
  if (typeStr === 'true') return { type: 'boolean', const: true };
  if (typeStr === 'any' || typeStr === 'object') return { type: 'object' };

  // Reference to another interface
  if (/^\w+$/.test(typeStr) && allDecls[typeStr]) {
    return { $ref: `#/components/schemas/${typeStr}` };
  }

  // Fallback
  return { type: 'string' };
}

/** Build an OpenAPI schema for a declaration. */
function buildSchema(name) {
  const decl = allDecls[name];
  if (!decl) return null;

  // Type alias (discriminated union)
  if (decl.isType) {
    const parts = decl.typeValue.split('|').map(s => s.trim()).filter(Boolean);
    // Check if it's a union of known interfaces
    const refs = parts.filter(p => allDecls[p]);
    if (refs.length === parts.length && refs.length > 1) {
      return { oneOf: refs.map(r => ({ $ref: `#/components/schemas/${r}` })) };
    }
    // Simple type alias
    if (parts.length === 1) {
      return tsTypeToSchema(parts[0]);
    }
    return tsTypeToSchema(decl.typeValue);
  }

  const fields = resolveFields(name);
  const properties = {};
  const required = [];
  let additionalProperties = null;

  for (const field of fields) {
    if (field.isIndex) {
      additionalProperties = tsTypeToSchema(field.type);
      continue;
    }
    properties[field.name] = tsTypeToSchema(field.type);
    if (field.description) {
      properties[field.name].description = field.description;
    }
    if (!field.optional) {
      required.push(field.name);
    }
  }

  const schema = { type: 'object' };
  if (required.length > 0) schema.required = required;
  schema.properties = properties;
  if (additionalProperties) schema.additionalProperties = additionalProperties;
  return schema;
}

// ---------------------------------------------------------------------------
// 4. Determine which schemas to include
// ---------------------------------------------------------------------------

// The API-facing types we want as top-level schemas:
const API_SCHEMAS = [
  // Translation types
  'ApiTranslation', 'ApiTranslationBook', 'ApiTranslationBookChapter',
  'ApiTranslationComplete', 'ApiTranslationCompleteBook',
  // Commentary types
  'ApiCommentary', 'ApiCommentaryBook', 'ApiCommentaryBookChapter',
  'ApiCommentaryProfile', 'ApiCommentaryProfileContent',
  // Dataset types
  'ApiDataset', 'ApiDatasetBook', 'ApiDatasetBookChapter',
  // Chapter data
  'ChapterData', 'CommentaryChapterData',
  'ChapterContent', 'ChapterHeading', 'ChapterLineBreak',
  'ChapterVerse', 'ChapterHebrewSubtitle',
  'FormattedText', 'InlineHeading', 'InlineLineBreak',
  'VerseFootnoteReference', 'ChapterFootnote',
  'TranslationBookChapterAudioLinks',
  // Dataset chapter data
  'DatasetChapterData', 'DatasetChapterVerseContent', 'ScoredVerseRef',
  // Commentary profile
  'CommentaryProfile',
];

// Build all schemas
const schemas = {};
for (const name of API_SCHEMAS) {
  const schema = buildSchema(name);
  if (schema) {
    schemas[name] = schema;
  }
}

// ---------------------------------------------------------------------------
// 5. YAML serializer
// ---------------------------------------------------------------------------

function toYaml(obj, indent = 0) {
  if (obj === null) return 'null';
  if (obj === undefined) return 'null';
  if (typeof obj === 'boolean') return obj.toString();
  if (typeof obj === 'number') return obj.toString();
  if (typeof obj === 'string') {
    // Needs quoting if it contains special chars, is a boolean/null word, or is empty
    if (/^(true|false|null|yes|no)$/i.test(obj) || obj === '' ||
        /[:#\[\]{}&*!|>'"%@`]/.test(obj) || /^\d/.test(obj) ||
        obj.includes('\n')) {
      return "'" + obj.replace(/'/g, "''") + "'";
    }
    return obj;
  }
  if (Array.isArray(obj)) {
    if (obj.length === 0) return '[]';
    // Use flow style for simple arrays (strings/numbers only, short)
    if (obj.every(i => typeof i === 'string' || typeof i === 'number') && JSON.stringify(obj).length < 60) {
      return '[' + obj.map(i => {
        if (typeof i === 'string') {
          if (/^(true|false|null)$/i.test(i) || /[:#\[\]{}&*!|>'"%@`]/.test(i)) {
            return "'" + i.replace(/'/g, "''") + "'";
          }
          return i;
        }
        return String(i);
      }).join(', ') + ']';
    }
    const pad = '  '.repeat(indent);
    return obj.map(item => {
      if (typeof item === 'object' && item !== null && !Array.isArray(item)) {
        const entries = Object.entries(item);
        if (entries.length === 0) return `${pad}- {}`;
        const first = entries[0];
        let result = `${pad}- ${first[0]}: ${toYaml(first[1], indent + 2)}`;
        for (let j = 1; j < entries.length; j++) {
          const val = toYaml(entries[j][1], indent + 2);
          if (typeof entries[j][1] === 'object' && entries[j][1] !== null && !isFlowable(entries[j][1])) {
            result += `\n${pad}  ${entries[j][0]}:\n${val}`;
          } else {
            result += `\n${pad}  ${entries[j][0]}: ${val}`;
          }
        }
        return result;
      }
      return `${pad}- ${toYaml(item, indent + 1)}`;
    }).join('\n');
  }
  // Object
  const entries = Object.entries(obj);
  if (entries.length === 0) return '{}';
  const pad = '  '.repeat(indent);
  return entries.map(([key, value]) => {
    if (value === undefined) return null;
    const yamlKey = /[:#\[\]{}&*!|>'"%@`,]/.test(key) || /^\d/.test(key) ? `'${key}'` : key;
    if (typeof value === 'object' && value !== null && !isFlowable(value)) {
      return `${pad}${yamlKey}:\n${toYaml(value, indent + 1)}`;
    }
    return `${pad}${yamlKey}: ${toYaml(value, indent + 1)}`;
  }).filter(Boolean).join('\n');
}

function isFlowable(obj) {
  if (Array.isArray(obj)) {
    return obj.every(i => typeof i === 'string' || typeof i === 'number') && JSON.stringify(obj).length < 60;
  }
  return false;
}

// ---------------------------------------------------------------------------
// 6. Build the OpenAPI document
// ---------------------------------------------------------------------------

// Rename Api-prefixed schemas to friendlier names for the spec
const SCHEMA_RENAMES = {
  'ApiTranslation': 'Translation',
  'ApiTranslationBook': 'TranslationBook',
  'ApiTranslationBookChapter': 'ChapterResponse',
  'ApiTranslationComplete': 'TranslationComplete',
  'ApiTranslationCompleteBook': 'TranslationCompleteBook',
  'ApiCommentary': 'Commentary',
  'ApiCommentaryBook': 'CommentaryBook',
  'ApiCommentaryBookChapter': 'CommentaryBookChapter',
  'ApiCommentaryProfile': 'CommentaryProfileRef',
  'ApiCommentaryProfileContent': 'CommentaryProfileContent',
  'ApiDataset': 'Dataset',
  'ApiDatasetBook': 'DatasetBook',
  'ApiDatasetBookChapter': 'DatasetBookChapter',
  'ChapterData': 'Chapter',
  'CommentaryChapterData': 'CommentaryChapter',
  'TranslationBookChapterAudioLinks': 'AudioLinks',
  'DatasetChapterData': 'DatasetChapterData',
  'DatasetChapterVerseContent': 'DatasetChapterVerseContent',
  'ScoredVerseRef': 'CrossReference',
  'CommentaryProfile': 'CommentaryProfile',
};

function renameRef(ref) {
  const name = ref.replace('#/components/schemas/', '');
  const renamed = SCHEMA_RENAMES[name] || name;
  return `#/components/schemas/${renamed}`;
}

/** Recursively rename $ref values in a schema. */
function renameRefs(schema) {
  if (!schema || typeof schema !== 'object') return schema;
  if (Array.isArray(schema)) return schema.map(renameRefs);
  const result = {};
  for (const [k, v] of Object.entries(schema)) {
    if (k === '$ref' && typeof v === 'string') {
      result[k] = renameRef(v);
    } else {
      result[k] = renameRefs(v);
    }
  }
  return result;
}

// Build renamed schemas
const renamedSchemas = {};
for (const [origName, schema] of Object.entries(schemas)) {
  const newName = SCHEMA_RENAMES[origName] || origName;
  renamedSchemas[newName] = renameRefs(schema);
}

// Add discriminator to ChapterContent if present
if (renamedSchemas['ChapterContent']) {
  renamedSchemas['ChapterContent'].discriminator = {
    propertyName: 'type',
    mapping: {
      verse: '#/components/schemas/ChapterVerse',
      heading: '#/components/schemas/ChapterHeading',
      line_break: '#/components/schemas/ChapterLineBreak',
      hebrew_subtitle: '#/components/schemas/ChapterHebrewSubtitle',
    },
  };
}

// Fix AudioLinks — should be additionalProperties map, not regular properties
renamedSchemas['AudioLinks'] = {
  type: 'object',
  description: 'Map of reader name to audio file URL',
  additionalProperties: { type: 'string', format: 'uri' },
};

// Add VerseContentItem union schema
renamedSchemas['VerseContentItem'] = {
  description: 'A single element within verse content',
  oneOf: [
    { type: 'string' },
    { $ref: '#/components/schemas/FormattedText' },
    { $ref: '#/components/schemas/VerseFootnoteReference' },
    { $ref: '#/components/schemas/InlineHeading' },
    { $ref: '#/components/schemas/InlineLineBreak' },
  ],
};

// Fix ChapterVerse content items to reference VerseContentItem
if (renamedSchemas['ChapterVerse'] && renamedSchemas['ChapterVerse'].properties &&
    renamedSchemas['ChapterVerse'].properties.content) {
  renamedSchemas['ChapterVerse'].properties.content = {
    type: 'array',
    items: { $ref: '#/components/schemas/VerseContentItem' },
  };
}

// Fix ChapterHebrewSubtitle content items
if (renamedSchemas['ChapterHebrewSubtitle'] && renamedSchemas['ChapterHebrewSubtitle'].properties &&
    renamedSchemas['ChapterHebrewSubtitle'].properties.content) {
  renamedSchemas['ChapterHebrewSubtitle'].properties.content = {
    type: 'array',
    items: {
      oneOf: [
        { type: 'string' },
        { $ref: '#/components/schemas/FormattedText' },
        { $ref: '#/components/schemas/VerseFootnoteReference' },
      ],
    },
  };
}

// Fix Chapter content to reference ChapterContent
if (renamedSchemas['Chapter'] && renamedSchemas['Chapter'].properties &&
    renamedSchemas['Chapter'].properties.content) {
  renamedSchemas['Chapter'].properties.content = {
    type: 'array',
    items: { $ref: '#/components/schemas/ChapterContent' },
  };
}

// Fix CommentaryChapter content
if (renamedSchemas['CommentaryChapter'] && renamedSchemas['CommentaryChapter'].properties &&
    renamedSchemas['CommentaryChapter'].properties.content) {
  renamedSchemas['CommentaryChapter'].properties.content = {
    type: 'array',
    items: { $ref: '#/components/schemas/ChapterVerse' },
  };
}

// Fix ChapterHeading content — should be array of strings
if (renamedSchemas['ChapterHeading'] && renamedSchemas['ChapterHeading'].properties &&
    renamedSchemas['ChapterHeading'].properties.content) {
  renamedSchemas['ChapterHeading'].properties.content = {
    type: 'array',
    items: { type: 'string' },
    description: 'Heading text segments; concatenate with a space',
  };
}

// Fix CrossReference — add book field (from VerseRef parent which is imported externally)
if (renamedSchemas['CrossReference']) {
  renamedSchemas['CrossReference'] = {
    type: 'object',
    required: ['book', 'chapter', 'verse', 'score'],
    properties: {
      book: { type: 'string', description: 'USFM book identifier' },
      chapter: { type: 'integer', description: 'Chapter number' },
      verse: { type: 'integer', description: 'Verse number' },
      score: { type: 'number', description: 'Relevance score' },
    },
  };
}

// Fix CommentaryProfile — reference field is VerseRef which is external
if (renamedSchemas['CommentaryProfile']) {
  if (renamedSchemas['CommentaryProfile'].properties &&
      renamedSchemas['CommentaryProfile'].properties.reference) {
    renamedSchemas['CommentaryProfile'].properties.reference = {
      oneOf: [
        {
          type: 'object',
          required: ['book', 'chapter', 'verse'],
          properties: {
            book: { type: 'string' },
            chapter: { type: 'integer' },
            verse: { type: 'integer' },
          },
        },
        { type: 'null' },
      ],
    };
  }
}

// Fix CommentaryProfileRef — inherit from CommentaryProfile + add links
if (renamedSchemas['CommentaryProfileRef']) {
  renamedSchemas['CommentaryProfileRef'] = {
    type: 'object',
    required: ['id', 'subject', 'thisProfileLink'],
    properties: {
      id: { type: 'string' },
      subject: { type: 'string' },
      reference: {
        oneOf: [
          {
            type: 'object',
            properties: {
              book: { type: 'string' },
              chapter: { type: 'integer' },
              verse: { type: 'integer' },
            },
          },
          { type: 'null' },
        ],
      },
      thisProfileLink: { type: 'string' },
      referenceChapterLink: { type: ['string', 'null'] },
    },
  };
}

// Build the parameters
const parameters = {
  translationId: {
    name: 'translationId',
    in: 'path',
    required: true,
    description: 'Translation identifier (e.g. BSB, KJV)',
    schema: { type: 'string' },
  },
  bookId: {
    name: 'bookId',
    in: 'path',
    required: true,
    description: 'USFM book identifier (e.g. GEN, JHN, REV)',
    schema: { type: 'string' },
  },
  chapter: {
    name: 'chapter',
    in: 'path',
    required: true,
    description: 'Chapter number',
    schema: { type: 'integer', minimum: 1 },
  },
  commentaryId: {
    name: 'commentaryId',
    in: 'path',
    required: true,
    description: 'Commentary identifier',
    schema: { type: 'string' },
  },
  datasetId: {
    name: 'datasetId',
    in: 'path',
    required: true,
    description: 'Dataset identifier',
    schema: { type: 'string' },
  },
  profileId: {
    name: 'profileId',
    in: 'path',
    required: true,
    description: 'Profile identifier',
    schema: { type: 'string' },
  },
};

// Build the paths from the endpoint patterns in api.ts
const paths = {
  '/api/available_translations.json': {
    get: {
      operationId: 'listTranslations',
      summary: 'List all available translations',
      responses: {
        '200': {
          description: 'List of translations',
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['translations'],
                properties: {
                  translations: {
                    type: 'array',
                    items: { $ref: '#/components/schemas/Translation' },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
  '/api/{translationId}/books.json': {
    get: {
      operationId: 'listBooks',
      summary: 'List books for a translation',
      parameters: [{ $ref: '#/components/parameters/translationId' }],
      responses: {
        '200': {
          description: 'Translation info and list of books',
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['translation', 'books'],
                properties: {
                  translation: { $ref: '#/components/schemas/Translation' },
                  books: {
                    type: 'array',
                    items: { $ref: '#/components/schemas/TranslationBook' },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
  '/api/{translationId}/{bookId}/{chapter}.json': {
    get: {
      operationId: 'getChapter',
      summary: 'Get chapter content',
      parameters: [
        { $ref: '#/components/parameters/translationId' },
        { $ref: '#/components/parameters/bookId' },
        { $ref: '#/components/parameters/chapter' },
      ],
      responses: {
        '200': {
          description: 'Chapter content with navigation links',
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/ChapterResponse' },
            },
          },
        },
      },
    },
  },
  '/api/{translationId}/complete.json': {
    get: {
      operationId: 'getCompleteTranslation',
      summary: 'Get complete translation download',
      parameters: [{ $ref: '#/components/parameters/translationId' }],
      responses: {
        '200': {
          description: 'Complete translation with all books and chapters',
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/TranslationComplete' },
            },
          },
        },
      },
    },
  },
  '/api/available_commentaries.json': {
    get: {
      operationId: 'listCommentaries',
      summary: 'List all available commentaries',
      responses: {
        '200': {
          description: 'List of commentaries',
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['commentaries'],
                properties: {
                  commentaries: {
                    type: 'array',
                    items: { $ref: '#/components/schemas/Commentary' },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
  '/api/c/{commentaryId}/books.json': {
    get: {
      operationId: 'listCommentaryBooks',
      summary: 'List books for a commentary',
      parameters: [{ $ref: '#/components/parameters/commentaryId' }],
      responses: {
        '200': {
          description: 'Commentary info and list of books',
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['commentary', 'books'],
                properties: {
                  commentary: { $ref: '#/components/schemas/Commentary' },
                  books: {
                    type: 'array',
                    items: { $ref: '#/components/schemas/CommentaryBook' },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
  '/api/c/{commentaryId}/{bookId}/{chapter}.json': {
    get: {
      operationId: 'getCommentaryChapter',
      summary: 'Get commentary chapter content',
      parameters: [
        { $ref: '#/components/parameters/commentaryId' },
        { $ref: '#/components/parameters/bookId' },
        { $ref: '#/components/parameters/chapter' },
      ],
      responses: {
        '200': {
          description: 'Commentary chapter content with navigation links',
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/CommentaryBookChapter' },
            },
          },
        },
      },
    },
  },
  '/api/c/{commentaryId}/profiles.json': {
    get: {
      operationId: 'listCommentaryProfiles',
      summary: 'List profiles for a commentary',
      parameters: [{ $ref: '#/components/parameters/commentaryId' }],
      responses: {
        '200': {
          description: 'Commentary info and list of profiles',
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['commentary', 'profiles'],
                properties: {
                  commentary: { $ref: '#/components/schemas/Commentary' },
                  profiles: {
                    type: 'array',
                    items: { $ref: '#/components/schemas/CommentaryProfileRef' },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
  '/api/c/{commentaryId}/profiles/{profileId}.json': {
    get: {
      operationId: 'getCommentaryProfile',
      summary: 'Get commentary profile content',
      parameters: [
        { $ref: '#/components/parameters/commentaryId' },
        { $ref: '#/components/parameters/profileId' },
      ],
      responses: {
        '200': {
          description: 'Commentary profile content',
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/CommentaryProfileContent' },
            },
          },
        },
      },
    },
  },
  '/api/available_datasets.json': {
    get: {
      operationId: 'listDatasets',
      summary: 'List all available datasets',
      responses: {
        '200': {
          description: 'List of datasets',
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['datasets'],
                properties: {
                  datasets: {
                    type: 'array',
                    items: { $ref: '#/components/schemas/Dataset' },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
  '/api/d/{datasetId}/books.json': {
    get: {
      operationId: 'listDatasetBooks',
      summary: 'List books for a dataset',
      parameters: [{ $ref: '#/components/parameters/datasetId' }],
      responses: {
        '200': {
          description: 'Dataset info and list of books',
          content: {
            'application/json': {
              schema: {
                type: 'object',
                required: ['dataset', 'books'],
                properties: {
                  dataset: { $ref: '#/components/schemas/Dataset' },
                  books: {
                    type: 'array',
                    items: { $ref: '#/components/schemas/DatasetBook' },
                  },
                },
              },
            },
          },
        },
      },
    },
  },
  '/api/d/{datasetId}/{bookId}/{chapter}.json': {
    get: {
      operationId: 'getDatasetChapter',
      summary: 'Get dataset chapter content (cross-references)',
      parameters: [
        { $ref: '#/components/parameters/datasetId' },
        { $ref: '#/components/parameters/bookId' },
        { $ref: '#/components/parameters/chapter' },
      ],
      responses: {
        '200': {
          description: 'Dataset chapter content with cross-references',
          content: {
            'application/json': {
              schema: { $ref: '#/components/schemas/DatasetBookChapter' },
            },
          },
        },
      },
    },
  },
};

// Assemble the full OpenAPI document
const spec = {
  openapi: '3.1.0',
  info: {
    title: 'helloao.org Bible API',
    version: API_VERSION,
    description: 'Public, read-only JSON API serving Bible translations, books, and chapter content from helloao.org. No authentication required.',
    license: {
      name: 'Various (per translation)',
      url: 'https://helloao.org',
    },
    'x-api-source': 'https://bible.helloao.org',
    'x-api-changelog': 'https://github.com/HelloAOLab/bible-api/blob/main/API-CHANGELOG.md',
  },
  servers: [
    { url: 'https://bible.helloao.org', description: 'Production' },
  ],
  paths,
  components: {
    parameters,
    schemas: renamedSchemas,
  },
};

// ---------------------------------------------------------------------------
// 7. Output
// ---------------------------------------------------------------------------
if (process.argv.includes('--json')) {
  process.stdout.write(JSON.stringify(spec, null, 2) + '\n');
} else {
  process.stdout.write(toYaml(spec, 0) + '\n');
}
