# helloao.org Bible API Reference

Base URL: `https://bible.helloao.org/api`

No authentication required. No rate limits documented. Be respectful — batch sequential, don't parallel-blast.

## Endpoints

### List translations
`GET /api/available_translations.json`

Returns `{ translations: [...] }` where each translation has:
```json
{
  "id": "BSB",
  "name": "Berean Standard Bible",
  "shortName": "BSB",
  "englishName": "Berean Standard Bible",
  "language": "eng",
  "numberOfBooks": 66,
  "totalNumberOfVerses": 31086,
  "listOfBooksApiLink": "/api/BSB/books.json"
}
```

### List books for a translation
`GET /api/{TRANSLATION}/books.json`

Returns `{ books: [...] }` where each book has:
```json
{
  "id": "GEN",
  "name": "Genesis",
  "commonName": "Genesis",
  "order": 1,
  "numberOfChapters": 50,
  "totalNumberOfVerses": 1533,
  "firstChapterApiLink": "/api/BSB/GEN/1.json"
}
```

### Get chapter content
`GET /api/{TRANSLATION}/{BOOK_ID}/{CHAPTER}.json`

Example: `GET /api/BSB/JHN/3.json`

Response shape:
```json
{
  "translation": { "id": "BSB", "name": "Berean Standard Bible", ... },
  "book": { "id": "JHN", "name": "John", ... },
  "chapter": {
    "number": 3,
    "content": [ ... ],
    "footnotes": [ ... ]
  },
  "numberOfVerses": 36,
  "thisChapterLink": "/api/BSB/JHN/3.json",
  "nextChapterApiLink": "/api/BSB/JHN/4.json",
  "previousChapterApiLink": "/api/BSB/JHN/2.json"
}
```

### Chapter content array

`chapter.content` is an array of objects. Each object has a `type`:

**Verse:**
```json
{
  "type": "verse",
  "number": 16,
  "content": [
    "For God so loved the world that He gave His one and only",
    { "noteId": 17 },
    "Son, that everyone who believes in Him shall not perish but have eternal life."
  ]
}
```

**Heading:**
```json
{ "type": "heading", "content": ["Jesus and Nicodemus"] }
```

**Line break:**
```json
{ "type": "line_break" }
```

### Verse content parsing

The `content` array within a verse contains:
- **Strings** — actual text fragments. Concatenate them for the full verse text.
- **Objects with `noteId`** — footnote markers. Skip for clean text; include for study mode.
- **Other objects** — formatting hints. Extract any text content, skip metadata.

### Footnotes

`chapter.footnotes` is an array:
```json
{
  "noteId": 13,
  "caller": "+",
  "text": "Or born from above; also in verse 7.",
  "reference": { "chapter": 3, "verse": 3 }
}
```

Match `noteId` in verse content to footnotes array for study notes.

## Book ID Mapping

### Old Testament
| Book | ID | Book | ID |
|---|---|---|---|
| Genesis | GEN | Exodus | EXO |
| Leviticus | LEV | Numbers | NUM |
| Deuteronomy | DEU | Joshua | JOS |
| Judges | JDG | Ruth | RUT |
| 1 Samuel | 1SA | 2 Samuel | 2SA |
| 1 Kings | 1KI | 2 Kings | 2KI |
| 1 Chronicles | 1CH | 2 Chronicles | 2CH |
| Ezra | EZR | Nehemiah | NEH |
| Esther | EST | Job | JOB |
| Psalm | PSA | Proverbs | PRO |
| Ecclesiastes | ECC | Song of Solomon | SNG |
| Isaiah | ISA | Jeremiah | JER |
| Lamentations | LAM | Ezekiel | EZK |
| Daniel | DAN | Hosea | HOS |
| Joel | JOL | Amos | AMO |
| Obadiah | OBA | Jonah | JON |
| Micah | MIC | Nahum | NAM |
| Habakkuk | HAB | Zephaniah | ZEP |
| Haggai | HAG | Zechariah | ZEC |
| Malachi | MAL | | |

### New Testament
| Book | ID | Book | ID |
|---|---|---|---|
| Matthew | MAT | Mark | MRK |
| Luke | LUK | John | JHN |
| Acts | ACT | Romans | ROM |
| 1 Corinthians | 1CO | 2 Corinthians | 2CO |
| Galatians | GAL | Ephesians | EPH |
| Philippians | PHP | Colossians | COL |
| 1 Thessalonians | 1TH | 2 Thessalonians | 2TH |
| 1 Timothy | 1TI | 2 Timothy | 2TI |
| Titus | TIT | Philemon | PHM |
| Hebrews | HEB | James | JAS |
| 1 Peter | 1PE | 2 Peter | 2PE |
| 1 John | 1JN | 2 John | 2JN |
| 3 John | 3JN | Jude | JUD |
| Revelation | REV | | |

### Common aliases
- "Psalms" → PSA (always use singular "Psalm" in output)
- "Song of Songs" → SNG
- "Revelation of John" → REV
