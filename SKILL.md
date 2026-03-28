---
name: bible-bsb
description: "Look up Bible verses and chapters via the helloao.org API. Default translation: Berean Study Bible (BSB). Supports 1000+ translations. Use when the user asks for a Bible verse, passage, chapter, or Scripture reference."
metadata:
  openclaw:
    emoji: "📖"
---

# Bible Verse Lookup

You look up Bible verses using the helloao.org API. Default translation: **BSB** (Berean Standard Bible).

## 1. Parse the Reference

Extract **book**, **chapter**, and optional **verse(s)** from the user's request.

Examples:
- "John 3:16" → book=`JHN`, chapter=`3`, verse=`16`
- "1 Corinthians 13:4-8" → book=`1CO`, chapter=`13`, verses=`4-8`
- "Psalm 23" → book=`PSA`, chapter=`23`, verses=all
- "Gen 1:1" → book=`GEN`, chapter=`1`, verse=`1`

Handle numbered books: "1 Cor", "2 Tim", "3 John". The number is part of the book name, not the chapter.

## 2. Book ID Mapping

Map the book name to its 3-letter API ID. Common mappings:

**OT:** Genesis=GEN, Exodus=EXO, Leviticus=LEV, Numbers=NUM, Deuteronomy=DEU, Joshua=JOS, Judges=JDG, Ruth=RUT, 1 Samuel=1SA, 2 Samuel=2SA, 1 Kings=1KI, 2 Kings=2KI, 1 Chronicles=1CH, 2 Chronicles=2CH, Ezra=EZR, Nehemiah=NEH, Esther=EST, Job=JOB, Psalm=PSA, Proverbs=PRO, Ecclesiastes=ECC, Song of Solomon=SNG, Isaiah=ISA, Jeremiah=JER, Lamentations=LAM, Ezekiel=EZK, Daniel=DAN, Hosea=HOS, Joel=JOL, Amos=AMO, Obadiah=OBA, Jonah=JON, Micah=MIC, Nahum=NAM, Habakkuk=HAB, Zephaniah=ZEP, Haggai=HAG, Zechariah=ZEC, Malachi=MAL

**NT:** Matthew=MAT, Mark=MRK, Luke=LUK, John=JHN, Acts=ACT, Romans=ROM, 1 Corinthians=1CO, 2 Corinthians=2CO, Galatians=GAL, Ephesians=EPH, Philippians=PHP, Colossians=COL, 1 Thessalonians=1TH, 2 Thessalonians=2TH, 1 Timothy=1TI, 2 Timothy=2TI, Titus=TIT, Philemon=PHM, Hebrews=HEB, James=JAS, 1 Peter=1PE, 2 Peter=2PE, 1 John=1JN, 2 John=2JN, 3 John=3JN, Jude=JUD, Revelation=REV

**Aliases:** Psalms→PSA, Song of Songs→SNG, Revelation of John→REV

Match abbreviations by prefix: "Gen"→GEN, "Rev"→REV, "Phil"→PHP (not PHM — Philippians is more common; use "Phm" or "Philemon" for Philemon), "Cor"→1CO (ask which if ambiguous).

## 3. Fetch the Chapter

Use `web_fetch` to GET:
```
https://bible.helloao.org/api/{TRANSLATION}/{BOOK_ID}/{CHAPTER}.json
```

Example: `https://bible.helloao.org/api/BSB/JHN/3.json`

If the user requests a different translation (e.g. "KJV", "ESV"), swap it into the URL. To discover available translations, fetch: `https://bible.helloao.org/api/available_translations.json`

## 4. Parse the Response

The response has `chapter.content[]` — an array of objects with a `type` field:

- **`type: "verse"`** — has `number` (int) and `content` (array)
- **`type: "heading"`** — has `content` (array of strings) — use as section breaks
- **`type: "line_break"`** — paragraph/stanza separator

### Verse content parsing

Each verse's `content` array contains:
- **Strings** → actual text. Concatenate all strings for clean verse text.
- **Objects with `noteId`** → footnote markers. **Skip these** for clean output.
- **Other objects** → skip unless they contain a text string.

Example: content = `["For God so loved ", {"noteId": 17}, "the world..."]` → "For God so loved the world..."

## 5. Format the Output

### Single verse
```
"For God so loved the world that He gave His one and only Son, that everyone who believes in Him shall not perish but have eternal life."
— John 3:16 (BSB)
```

### Verse range (e.g. "1 Corinthians 13:4-8")
Filter verses where `number` is between start and end (inclusive). Prefix each with its number:

```
⁴ Love is patient, love is kind. It does not envy, it does not boast, it is not proud.
⁵ It does not dishonor others, it is not self-seeking, it is not easily angered, it keeps no account of wrongs.
⁶ Love does not delight in evil but rejoices with the truth.
⁷ It bears all things, believes all things, hopes all things, endures all things.
⁸ Love never fails.
— 1 Corinthians 13:4-8 (BSB)
```

Use superscript numbers (⁰¹²³⁴⁵⁶⁷⁸⁹) for verse numbers.

### Full chapter (e.g. "Psalm 23")
Include all verses with superscript numbers. Insert headings as **bold** section breaks:

```
**A Psalm of David**

¹ The LORD is my shepherd; I shall not want.
² He makes me lie down in green pastures; He leads me beside quiet waters.
...
— Psalm 23 (BSB)
```

### Formatting rules
- No markdown tables — output must be readable on Discord/WhatsApp.
- Use plain text with superscript verse numbers for ranges/chapters.
- Single verses get a simple quote with attribution.
- Keep output clean and readable.

## 6. Error Handling

- **API unreachable**: "I couldn't reach the Bible API right now. Please try again shortly."
- **404 / verse not found**: "I couldn't find {reference}. Please check the book name, chapter, and verse numbers." Suggest corrections if close (e.g. "Psalm 151" → "Psalms only has 150 chapters").
- **Unknown book name**: "I don't recognize '{book}' as a Bible book. Did you mean {suggestion}?"
- **Verse number out of range**: Check `numberOfVerses` in the response. If the requested verse exceeds it, tell the user the chapter only has N verses.

## 7. Translation Override

Default is BSB. If the user says "in KJV" or "ESV translation", use that translation ID in the URL instead of BSB. Include the translation in the attribution: `— John 3:16 (KJV)`

If unsure whether a translation exists, fetch `https://bible.helloao.org/api/available_translations.json` and check `translations[].id`.
