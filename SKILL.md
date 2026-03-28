---
name: bible-bsb
description: "Look up Bible verses and chapters via the helloao.org API. Default translation: Berean Study Bible (BSB). Supports 1000+ translations. Use when the user asks for a Bible verse, passage, chapter, or Scripture reference."
metadata:
  openclaw:
    emoji: "📖"
---

# Bible BSB — helloao.org API Integration

*Core instructions to be added in issue #2.*

## Footnotes and Study Notes

### Default mode (clean reading)
- When rendering verses, skip all `{noteId}` objects in the content array
- Concatenate only the string elements for clean text

### Study mode
- Activated when user asks for "study notes", "with footnotes", "detailed", or "annotated"
- After verse text, append matching footnotes as numbered notes
- Match `{noteId}` from verse content to `chapter.footnotes[]` by `noteId` field
- Insert a bracketed number in the text where each `{noteId}` appeared

**Normal:**
For God so loved the world that He gave His one and only Son... — John 3:16 (BSB)

**Study mode:**
For God so loved the world that He gave His one and only[1] Son... — John 3:16 (BSB)

Notes:
[1] Or only begotten (v.16)
