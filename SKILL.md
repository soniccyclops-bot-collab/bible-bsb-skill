---
name: bible-bsb
description: "Look up Bible verses and chapters via the helloao.org API. Default translation: Berean Study Bible (BSB). Supports 1000+ translations. Use when the user asks for a Bible verse, passage, chapter, or Scripture reference."
metadata:
  openclaw:
    emoji: "📖"
---

# Bible Verse Lookup

Use this skill when the user asks for a Bible verse, passage, chapter, or Scripture reference.

## How to use

Run the lookup script:

```bash
python3 {skill_dir}/scripts/bible_lookup.py "Reference" [--translation ID] [--study] [--compare] [--cross-refs] [--expand] [--commentary [NAME]]
```

### Arguments

- **reference** (required): A human-readable Bible reference.
  Examples: `"John 3:16"`, `"Romans 8:28-30"`, `"Psalm 23"`, `"1 Corinthians 13:4-8"`
- **--translation ID**: Translation code (default: `BSB`). Common options: `KJV`, `ENGWEBP`.
- **--study**: Include footnotes from the translation alongside verse text.
- **--compare**: Show the passage in BSB, KJV, and ENGWEBP side by side.
- **--cross-refs**: Show cross-references for the verse(s).
- **--expand**: Used with `--cross-refs` to fetch and display the text of each referenced verse. Limited to the first 8 references to be respectful of the API.
- **--commentary [NAME]**: Show commentary for the verse(s). Defaults to `john-gill` if no name given. Accepts partial name matches — the script dynamically queries the available commentaries API. Example: `--commentary gill`.

### Reference format

- Full names or common abbreviations: `Genesis 1:1`, `Gen 1:1`
- Numbered books: `1 Corinthians 13:4-8`, `2 Tim 1:7`
- Full chapter (no verse): `Psalm 23`
- Case-insensitive

### Output

The script prints:
1. A citation header (e.g., `John 3:16 (BSB)`)
2. Verse numbers and text, with section headings in brackets
3. If `--study`: footnotes listed at the end with reference and note text
4. If `--compare`: the same block repeated for each translation
5. If `--cross-refs`: a list of cross-references after the verse text
6. If `--cross-refs --expand`: cross-references with their full verse text
7. If `--commentary`: verse text followed by commentary text for those verses

### Error handling

The script exits with a non-zero code and prints to stderr on:
- Unknown book name
- Unparseable reference format
- API fetch failure (network or HTTP error)

### Notes

- No external dependencies — uses Python stdlib only.
- API: `https://bible.helloao.org/api` (no auth required).
- Be respectful of the API — do not blast parallel requests.
