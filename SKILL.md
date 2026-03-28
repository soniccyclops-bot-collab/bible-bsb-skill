---
name: bible-bsb
description: "Look up Bible verses and chapters via the helloao.org API. Default translation: Berean Study Bible (BSB). Supports 1000+ translations. Use when the user asks for a Bible verse, passage, chapter, or Scripture reference."
metadata:
  openclaw:
    emoji: "📖"
---

# Bible BSB — helloao.org API Integration

*Core instructions to be added in issue #2.*

## Cross-References

When providing context for a verse, suggest related passages the user may want to explore.

When the user asks "what does the Bible say about X," fetch and quote cross-referenced verses (not just cite them).

### Theme → Key Verses

| Theme | Key Verses |
|---|---|
| Faith | Hebrews 11:1, Romans 10:17, James 2:17 |
| Love | 1 Corinthians 13:4-7, John 3:16, 1 John 4:8 |
| Salvation | Ephesians 2:8-9, Romans 10:9, Acts 4:12 |
| Grace | 2 Corinthians 12:9, Ephesians 2:8, Titus 2:11 |
| Redemption | Colossians 1:13-14, Ephesians 1:7, 1 Peter 1:18-19 |
| Wisdom | Proverbs 9:10, James 1:5, Proverbs 3:5-6 |
| Prayer | Philippians 4:6-7, Matthew 6:9-13, 1 Thessalonians 5:16-18 |
| Suffering | Romans 8:28, James 1:2-4, 2 Corinthians 4:17 |

## Translation Comparison

When the user asks to "compare translations" or see a verse "in different translations":

1. Fetch the same chapter from multiple translations (serialize requests — do not fetch in parallel)
2. Extract the target verse from each response
3. Present side-by-side with translation labels

### Known Translation IDs

- **BSB** — Berean Standard Bible (default)
- **KJV** — King James Version
- **ENGWEBP** — World English Bible

If the user names a translation not listed above, fetch `available_translations.json` and search by `englishName` to resolve the correct ID.
