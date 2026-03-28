#!/usr/bin/env python3
"""
generate_client.py — Read an OpenAPI 3.1.0 JSON spec and generate a Python
client package with typed dataclasses and HTTP methods.

Usage: python3 scripts/generate_client.py <spec.json> <output_dir>

Only uses Python stdlib (no pip packages).
"""

import json
import os
import re
import sys
import textwrap


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def resolve_ref(spec, ref):
    """Resolve a $ref string like '#/components/schemas/Foo' against the spec."""
    parts = ref.lstrip('#/').split('/')
    node = spec
    for p in parts:
        if isinstance(node, dict):
            node = node.get(p)
        else:
            return None
    return node


def ref_name(ref_str):
    """Extract the schema name from a $ref string."""
    return ref_str.split('/')[-1]


def to_snake_case(name):
    """Convert camelCase/PascalCase to snake_case."""
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()


SIMPLE_TYPE_MAP = {
    'string': 'str',
    'integer': 'int',
    'number': 'float',
    'boolean': 'bool',
}


def schema_to_python_type(schema, spec):
    """Convert an OpenAPI schema to a Python type annotation string."""
    if schema is None:
        return 'Any'

    if '$ref' in schema:
        return ref_name(schema['$ref'])

    one_of = schema.get('oneOf')
    schema_type = schema.get('type')

    # oneOf handling
    if one_of:
        types = []
        has_null = False
        for s in one_of:
            if isinstance(s, dict) and s.get('type') == 'null':
                has_null = True
            else:
                types.append(schema_to_python_type(s, spec))
        if len(types) == 1 and has_null:
            return f'Optional[{types[0]}]'
        if len(types) > 1:
            type_str = f'Union[{", ".join(types)}]'
        elif types:
            type_str = types[0]
        else:
            type_str = 'Any'
        return f'Optional[{type_str}]' if has_null else type_str

    # type: [string, null] shorthand
    if isinstance(schema_type, list):
        non_null = [t for t in schema_type if t != 'null']
        has_null = 'null' in schema_type
        if len(non_null) == 1:
            base = SIMPLE_TYPE_MAP.get(non_null[0], 'Any')
            return f'Optional[{base}]' if has_null else base
        return 'Any'

    if schema_type == 'array':
        items = schema.get('items', {})
        item_type = schema_to_python_type(items, spec)
        return f'List[{item_type}]'

    if schema_type == 'object':
        if 'additionalProperties' in schema:
            val_type = schema_to_python_type(schema['additionalProperties'], spec)
            return f'Dict[str, {val_type}]'
        if 'properties' not in schema:
            return 'Dict[str, Any]'
        return 'Dict[str, Any]'

    if schema_type in SIMPLE_TYPE_MAP:
        return SIMPLE_TYPE_MAP[schema_type]

    return 'Any'


# ---------------------------------------------------------------------------
# models.py generator
# ---------------------------------------------------------------------------

def generate_models(spec):
    """Generate models.py content from component schemas."""
    schemas = spec.get('components', {}).get('schemas', {})

    lines = [
        '"""Auto-generated dataclass models from OpenAPI spec."""',
        'from __future__ import annotations',
        '',
        'from dataclasses import dataclass, field',
        'from typing import Any, Dict, List, Optional, Union',
        '',
        '',
    ]

    # Collect which schemas are object types with properties
    object_schemas = []
    alias_schemas = []

    for name, schema in schemas.items():
        if not isinstance(schema, dict):
            continue
        s_type = schema.get('type')
        one_of = schema.get('oneOf')

        # Union type alias (oneOf without being an object)
        if one_of and s_type != 'object':
            alias_schemas.append((name, schema))
            continue

        # Object with additionalProperties only (like AudioLinks) -> type alias
        if s_type == 'object' and 'properties' not in schema:
            alias_schemas.append((name, schema))
            continue

        if s_type == 'object' and 'properties' in schema:
            object_schemas.append((name, schema))

    # Generate dataclasses (before type aliases, since aliases reference classes)
    for name, schema in object_schemas:
        properties = schema.get('properties', {})
        required = set(schema.get('required', []))

        lines.append('@dataclass')
        lines.append(f'class {name}:')

        if not properties:
            lines.append('    pass')
            lines.append('')
            lines.append('')
            continue

        # Required fields first, then optional (dataclass ordering)
        req_fields = [(k, v) for k, v in properties.items() if k in required]
        opt_fields = [(k, v) for k, v in properties.items() if k not in required]

        for field_name, field_schema in req_fields:
            py_type = schema_to_python_type(field_schema, spec)
            py_field = to_snake_case(field_name)
            if py_field != field_name:
                lines.append(f'    {py_field}: {py_type}  # json: {field_name}')
            else:
                lines.append(f'    {py_field}: {py_type}')

        for field_name, field_schema in opt_fields:
            py_type = schema_to_python_type(field_schema, spec)
            py_field = to_snake_case(field_name)
            if not py_type.startswith('Optional'):
                py_type = f'Optional[{py_type}]'
            if py_field != field_name:
                lines.append(f'    {py_field}: {py_type} = None  # json: {field_name}')
            else:
                lines.append(f'    {py_field}: {py_type} = None')

        lines.append('')
        lines.append('')

    # Generate type aliases AFTER dataclasses (aliases reference class names)
    if alias_schemas:
        lines.append('')
        lines.append('# Type aliases (must be after class definitions)')
        for name, schema in alias_schemas:
            py_type = schema_to_python_type(schema, spec)
            lines.append(f'{name} = {py_type}')
        lines.append('')

    return '\n'.join(lines)


# ---------------------------------------------------------------------------
# client.py generator
# ---------------------------------------------------------------------------

def generate_client(spec):
    """Generate client.py content with BibleAPIClient."""
    servers = spec.get('servers', [])
    base_url = servers[0]['url'] if servers else 'https://bible.helloao.org'
    paths = spec.get('paths', {})

    lines = [
        '"""Auto-generated API client from OpenAPI spec."""',
        'from __future__ import annotations',
        '',
        'import json',
        'import urllib.request',
        'import urllib.error',
        'from typing import Any, Dict, List, Optional',
        '',
        'from .models import *',
        '',
        '',
        'class APIError(Exception):',
        '    """Raised when an API request fails."""',
        '',
        '    def __init__(self, message: str, status_code: Optional[int] = None):',
        '        super().__init__(message)',
        '        self.status_code = status_code',
        '',
        '',
    ]

    # Collect all schema names for from_dict helpers
    schemas = spec.get('components', {}).get('schemas', {})
    object_schema_names = set()
    for name, schema in schemas.items():
        if isinstance(schema, dict) and schema.get('type') == 'object' and 'properties' in schema:
            object_schema_names.add(name)

    lines.append('def _from_dict(cls, data):')
    lines.append('    """Recursively construct a dataclass from a dict."""')
    lines.append('    if data is None or not isinstance(data, dict):')
    lines.append('        return data')
    lines.append('    import dataclasses')
    lines.append('    if not dataclasses.is_dataclass(cls):')
    lines.append('        return data')
    lines.append('    field_map = {}')
    lines.append('    for f in dataclasses.fields(cls):')
    lines.append('        # Check the comment for the original json key name')
    lines.append('        field_map[f.name] = f')
    lines.append('    kwargs = {}')
    lines.append('    # Build reverse map: json_key -> field')
    lines.append('    json_to_field = {}')
    lines.append('    for f in dataclasses.fields(cls):')
    lines.append('        if f.metadata and "json_key" in f.metadata:')
    lines.append('            json_to_field[f.metadata["json_key"]] = f')
    lines.append('        json_to_field[f.name] = f')
    lines.append('    for key, val in data.items():')
    lines.append('        snake = key')
    lines.append('        # Try camelCase -> snake_case')
    lines.append('        import re')
    lines.append('        s1 = re.sub("(.)([A-Z][a-z]+)", r"\\1_\\2", key)')
    lines.append('        snake = re.sub("([a-z0-9])([A-Z])", r"\\1_\\2", s1).lower()')
    lines.append('        if snake in field_map:')
    lines.append('            kwargs[snake] = val')
    lines.append('        elif key in field_map:')
    lines.append('            kwargs[key] = val')
    lines.append('    return cls(**kwargs)')
    lines.append('')
    lines.append('')

    # Client class
    lines.append('class BibleAPIClient:')
    lines.append('    """Auto-generated client for the helloao.org Bible API."""')
    lines.append('')
    lines.append(f'    DEFAULT_BASE_URL = "{base_url}"')
    lines.append('')
    lines.append('    def __init__(self, base_url: Optional[str] = None, timeout: int = 30):')
    lines.append('        self.base_url = (base_url or self.DEFAULT_BASE_URL).rstrip("/")')
    lines.append('        self.timeout = timeout')
    lines.append('')
    lines.append('    def _request(self, path: str) -> Any:')
    lines.append('        """Make a GET request and return parsed JSON."""')
    lines.append('        url = self.base_url + path')
    lines.append('        req = urllib.request.Request(url, headers={"Accept": "application/json"})')
    lines.append('        try:')
    lines.append('            with urllib.request.urlopen(req, timeout=self.timeout) as resp:')
    lines.append('                return json.loads(resp.read().decode("utf-8"))')
    lines.append('        except urllib.error.HTTPError as e:')
    lines.append('            raise APIError(f"HTTP {e.code}: {e.reason}", e.code) from e')
    lines.append('        except urllib.error.URLError as e:')
    lines.append('            raise APIError(f"Connection error: {e.reason}") from e')
    lines.append('')

    # Generate methods for each endpoint
    for path_str, path_obj in paths.items():
        for method, op in path_obj.items():
            if method not in ('get', 'post', 'put', 'delete', 'patch'):
                continue
            op_id = op.get('operationId')
            if not op_id:
                continue

            method_name = to_snake_case(op_id)
            summary = op.get('summary', '')
            params = op.get('parameters', [])

            # Resolve parameter refs
            resolved_params = []
            for p in params:
                if '$ref' in p:
                    resolved = resolve_ref(spec, p['$ref'])
                    if resolved:
                        resolved_params.append(resolved)
                else:
                    resolved_params.append(p)

            # Build method signature
            param_parts = []
            for p in resolved_params:
                pname = to_snake_case(p['name'])
                ptype = SIMPLE_TYPE_MAP.get(p.get('schema', {}).get('type', 'string'), 'str')
                param_parts.append((pname, ptype, p['name']))

            sig_args = ['self'] + [f'{pn}: {pt}' for pn, pt, _ in param_parts]

            # Determine return type from response schema
            resp_200 = op.get('responses', {}).get('200', {})
            resp_schema = resp_200.get('content', {}).get('application/json', {}).get('schema', {})
            return_type = 'Dict[str, Any]'
            return_model = None
            if '$ref' in resp_schema:
                rname = ref_name(resp_schema['$ref'])
                if rname in object_schema_names:
                    return_type = rname
                    return_model = rname

            lines.append(f'    def {method_name}({", ".join(sig_args)}) -> {return_type}:')
            lines.append(f'        """{summary}"""')

            # Build URL path
            url_path = path_str
            for pn, _, orig in param_parts:
                url_path = url_path.replace('{' + orig + '}', '{' + pn + '}')

            if param_parts:
                lines.append(f'        path = f"{url_path}"')
            else:
                lines.append(f'        path = "{url_path}"')

            if return_model:
                lines.append(f'        return _from_dict({return_model}, self._request(path))')
            else:
                lines.append(f'        return self._request(path)')
            lines.append('')

    return '\n'.join(lines)


# ---------------------------------------------------------------------------
# __init__.py generator
# ---------------------------------------------------------------------------

def generate_init(spec):
    """Generate __init__.py that re-exports the client and models."""
    schemas = spec.get('components', {}).get('schemas', {})

    model_names = []
    for name, schema in schemas.items():
        if not isinstance(schema, dict):
            continue
        if schema.get('type') == 'object' and 'properties' in schema:
            model_names.append(name)

    lines = [
        '"""Auto-generated Python client for the helloao.org Bible API."""',
        '',
        'from .client import APIError, BibleAPIClient, _from_dict',
        'from .models import *',
        '',
        f'__all__ = ["APIError", "BibleAPIClient"] + {model_names!r}',
        '',
    ]
    return '\n'.join(lines)


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    if len(sys.argv) < 3:
        print(f'Usage: {sys.argv[0]} <spec.json> <output_dir>', file=sys.stderr)
        sys.exit(1)

    spec_path = sys.argv[1]
    output_dir = sys.argv[2]

    with open(spec_path, 'r') as f:
        spec = json.load(f)

    os.makedirs(output_dir, exist_ok=True)

    # Generate models.py
    models_path = os.path.join(output_dir, 'models.py')
    with open(models_path, 'w') as f:
        f.write(generate_models(spec))
    print(f'Generated {models_path}', file=sys.stderr)

    # Generate client.py
    client_path = os.path.join(output_dir, 'client.py')
    with open(client_path, 'w') as f:
        f.write(generate_client(spec))
    print(f'Generated {client_path}', file=sys.stderr)

    # Generate __init__.py
    init_path = os.path.join(output_dir, '__init__.py')
    with open(init_path, 'w') as f:
        f.write(generate_init(spec))
    print(f'Generated {init_path}', file=sys.stderr)


if __name__ == '__main__':
    main()
