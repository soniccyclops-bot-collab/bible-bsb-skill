---
name: bible-bsb
description: "Look up Bible verses and chapters via the helloao.org API. Default translation: Berean Study Bible (BSB). Supports 1000+ translations, 6 commentaries, and 344k cross-references. Use when the user asks for a Bible verse, passage, chapter, or Scripture reference."
metadata:
  openclaw:
    emoji: "📖"
    requires:
      anyBins: ["bb"]
---

# Bible BSB — helloao.org API

Look up verses, commentaries, and cross-references. Requires [Babashka](https://babashka.org/) (`bb`).

## Usage

Run from the skill directory. Replace `{skill_dir}` with the resolved skill path.

```bash
# Single verse
bb {skill_dir}/scripts/bible_lookup.clj "John 3:16"

# Verse range
bb {skill_dir}/scripts/bible_lookup.clj "Romans 8:28-30"

# Full chapter
bb {skill_dir}/scripts/bible_lookup.clj "Psalm 23"

# Different translation
bb {skill_dir}/scripts/bible_lookup.clj "John 3:16" --translation KJV

# Study mode (include footnotes)
bb {skill_dir}/scripts/bible_lookup.clj "John 3:16" --study

# Compare translations (BSB + KJV + WEB side by side)
bb {skill_dir}/scripts/bible_lookup.clj "John 3:16" --compare

# Commentary (dynamically resolved from API)
bb {skill_dir}/scripts/bible_lookup.clj "Psalm 32:1" --commentary gill
bb {skill_dir}/scripts/bible_lookup.clj "Psalm 32:1" --commentary henry
bb {skill_dir}/scripts/bible_lookup.clj "Psalm 32:1" --commentary tyndale

# Cross-references
bb {skill_dir}/scripts/bible_lookup.clj "Psalm 32:5" --cross-refs

# Cross-references with verse text
bb {skill_dir}/scripts/bible_lookup.clj "Psalm 32:5" --cross-refs --expand

# Combine flags
bb {skill_dir}/scripts/bible_lookup.clj "Romans 8:28" --study --cross-refs --expand
```

## When to use

- User asks for a Bible verse, passage, or chapter
- User asks for commentary or "what does X mean"
- User asks for cross-references or "related verses"
- User asks to compare translations
- User asks about a biblical topic (look up key verses)

## Available commentaries (resolved dynamically)

John Gill, Matthew Henry, Adam Clarke, Jamieson-Fausset-Brown, Keil & Delitzsch (OT), Tyndale Study Notes. Use any partial name match.

## Translation IDs

Default: BSB. Common: KJV (→ eng_kjv), WEB (→ ENGWEBP). Any valid translation ID from the API works.
