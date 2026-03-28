#!/usr/bin/env python3
"""Bible verse lookup via the helloao.org API."""

import argparse
import json
import re
import sys
import urllib.request
import urllib.error

BASE_URL = "https://bible.helloao.org/api"

# Common shorthand → actual API translation IDs
TRANSLATION_ALIASES = {
    "KJV": "eng_kjv",
    "WEB": "ENGWEBP",
    "ENGWEBP": "ENGWEBP",
    "BSB": "BSB",
    "ESV": "ESV",
    "NIV": "NIV",
    "KJA": "eng_kja",
}


BOOK_IDS = {
    "genesis": "GEN", "gen": "GEN",
    "exodus": "EXO", "exo": "EXO", "exod": "EXO",
    "leviticus": "LEV", "lev": "LEV",
    "numbers": "NUM", "num": "NUM",
    "deuteronomy": "DEU", "deu": "DEU", "deut": "DEU",
    "joshua": "JOS", "jos": "JOS", "josh": "JOS",
    "judges": "JDG", "jdg": "JDG", "judg": "JDG",
    "ruth": "RUT", "rut": "RUT",
    "1 samuel": "1SA", "1samuel": "1SA", "1 sam": "1SA", "1sam": "1SA",
    "2 samuel": "2SA", "2samuel": "2SA", "2 sam": "2SA", "2sam": "2SA",
    "1 kings": "1KI", "1kings": "1KI", "1 kgs": "1KI", "1kgs": "1KI",
    "2 kings": "2KI", "2kings": "2KI", "2 kgs": "2KI", "2kgs": "2KI",
    "1 chronicles": "1CH", "1chronicles": "1CH", "1 chr": "1CH", "1chr": "1CH",
    "2 chronicles": "2CH", "2chronicles": "2CH", "2 chr": "2CH", "2chr": "2CH",
    "ezra": "EZR", "ezr": "EZR",
    "nehemiah": "NEH", "neh": "NEH",
    "esther": "EST", "est": "EST",
    "job": "JOB",
    "psalm": "PSA", "psalms": "PSA", "psa": "PSA", "ps": "PSA",
    "proverbs": "PRO", "pro": "PRO", "prov": "PRO",
    "ecclesiastes": "ECC", "ecc": "ECC", "eccl": "ECC",
    "song of solomon": "SNG", "song of songs": "SNG", "sng": "SNG", "sos": "SNG",
    "isaiah": "ISA", "isa": "ISA",
    "jeremiah": "JER", "jer": "JER",
    "lamentations": "LAM", "lam": "LAM",
    "ezekiel": "EZK", "ezk": "EZK", "eze": "EZK",
    "daniel": "DAN", "dan": "DAN",
    "hosea": "HOS", "hos": "HOS",
    "joel": "JOL", "jol": "JOL",
    "amos": "AMO", "amo": "AMO",
    "obadiah": "OBA", "oba": "OBA",
    "jonah": "JON", "jon": "JON",
    "micah": "MIC", "mic": "MIC",
    "nahum": "NAM", "nam": "NAM", "nah": "NAM",
    "habakkuk": "HAB", "hab": "HAB",
    "zephaniah": "ZEP", "zep": "ZEP",
    "haggai": "HAG", "hag": "HAG",
    "zechariah": "ZEC", "zec": "ZEC", "zech": "ZEC",
    "malachi": "MAL", "mal": "MAL",
    "matthew": "MAT", "mat": "MAT", "matt": "MAT",
    "mark": "MRK", "mrk": "MRK",
    "luke": "LUK", "luk": "LUK",
    "john": "JHN", "jhn": "JHN", "jn": "JHN",
    "acts": "ACT", "act": "ACT",
    "romans": "ROM", "rom": "ROM",
    "1 corinthians": "1CO", "1corinthians": "1CO", "1 cor": "1CO", "1cor": "1CO",
    "2 corinthians": "2CO", "2corinthians": "2CO", "2 cor": "2CO", "2cor": "2CO",
    "galatians": "GAL", "gal": "GAL",
    "ephesians": "EPH", "eph": "EPH",
    "philippians": "PHP", "php": "PHP", "phil": "PHP",
    "colossians": "COL", "col": "COL",
    "1 thessalonians": "1TH", "1thessalonians": "1TH", "1 thess": "1TH", "1thess": "1TH",
    "2 thessalonians": "2TH", "2thessalonians": "2TH", "2 thess": "2TH", "2thess": "2TH",
    "1 timothy": "1TI", "1timothy": "1TI", "1 tim": "1TI", "1tim": "1TI",
    "2 timothy": "2TI", "2timothy": "2TI", "2 tim": "2TI", "2tim": "2TI",
    "titus": "TIT", "tit": "TIT",
    "philemon": "PHM", "phm": "PHM", "phlm": "PHM",
    "hebrews": "HEB", "heb": "HEB",
    "james": "JAS", "jas": "JAS",
    "1 peter": "1PE", "1peter": "1PE", "1 pet": "1PE", "1pet": "1PE",
    "2 peter": "2PE", "2peter": "2PE", "2 pet": "2PE", "2pet": "2PE",
    "1 john": "1JN", "1john": "1JN", "1 jn": "1JN", "1jn": "1JN",
    "2 john": "2JN", "2john": "2JN", "2 jn": "2JN", "2jn": "2JN",
    "3 john": "3JN", "3john": "3JN", "3 jn": "3JN", "3jn": "3JN",
    "jude": "JUD", "jud": "JUD",
    "revelation": "REV", "rev": "REV", "revelation of john": "REV",
}

# Reverse map: book ID -> display name
BOOK_NAMES = {
    "GEN": "Genesis", "EXO": "Exodus", "LEV": "Leviticus", "NUM": "Numbers",
    "DEU": "Deuteronomy", "JOS": "Joshua", "JDG": "Judges", "RUT": "Ruth",
    "1SA": "1 Samuel", "2SA": "2 Samuel", "1KI": "1 Kings", "2KI": "2 Kings",
    "1CH": "1 Chronicles", "2CH": "2 Chronicles", "EZR": "Ezra", "NEH": "Nehemiah",
    "EST": "Esther", "JOB": "Job", "PSA": "Psalm", "PRO": "Proverbs",
    "ECC": "Ecclesiastes", "SNG": "Song of Solomon", "ISA": "Isaiah", "JER": "Jeremiah",
    "LAM": "Lamentations", "EZK": "Ezekiel", "DAN": "Daniel", "HOS": "Hosea",
    "JOL": "Joel", "AMO": "Amos", "OBA": "Obadiah", "JON": "Jonah",
    "MIC": "Micah", "NAM": "Nahum", "HAB": "Habakkuk", "ZEP": "Zephaniah",
    "HAG": "Haggai", "ZEC": "Zechariah", "MAL": "Malachi",
    "MAT": "Matthew", "MRK": "Mark", "LUK": "Luke", "JHN": "John",
    "ACT": "Acts", "ROM": "Romans", "1CO": "1 Corinthians", "2CO": "2 Corinthians",
    "GAL": "Galatians", "EPH": "Ephesians", "PHP": "Philippians", "COL": "Colossians",
    "1TH": "1 Thessalonians", "2TH": "2 Thessalonians", "1TI": "1 Timothy",
    "2TI": "2 Timothy", "TIT": "Titus", "PHM": "Philemon", "HEB": "Hebrews",
    "JAS": "James", "1PE": "1 Peter", "2PE": "2 Peter", "1JN": "1 John",
    "2JN": "2 John", "3JN": "3 John", "JUD": "Jude", "REV": "Revelation",
}


def parse_reference(ref):
    """Parse a human-readable reference into (book_id, chapter, verse_start, verse_end)."""
    ref = ref.strip()
    # Match: optional number prefix + book name, then chapter:verse-verse or chapter
    m = re.match(
        r'^(\d?\s*[A-Za-z][A-Za-z\s]+?)\s+(\d+)(?::(\d+)(?:-(\d+))?)?$',
        ref
    )
    if not m:
        print(f"Error: Could not parse reference '{ref}'", file=sys.stderr)
        sys.exit(1)

    book_raw = m.group(1).strip()
    chapter = int(m.group(2))
    verse_start = int(m.group(3)) if m.group(3) else None
    verse_end = int(m.group(4)) if m.group(4) else verse_start

    book_key = book_raw.lower()
    book_id = BOOK_IDS.get(book_key)
    if not book_id:
        # Try collapsing spaces for numbered books: "1 sam" already covered,
        # but try without space: "1sam"
        collapsed = re.sub(r'^(\d)\s+', r'\1', book_key)
        book_id = BOOK_IDS.get(collapsed)
    if not book_id:
        print(f"Error: Unknown book '{book_raw}'", file=sys.stderr)
        sys.exit(1)

    return book_id, chapter, verse_start, verse_end


def resolve_translation(translation):
    """Resolve translation alias to API ID."""
    return TRANSLATION_ALIASES.get(translation.upper(), translation)

def fetch_chapter(translation, book_id, chapter):
    """Fetch chapter JSON from the API."""
    api_id = resolve_translation(translation)
    url = f"{BASE_URL}/{api_id}/{book_id}/{chapter}.json"
    try:
        with urllib.request.urlopen(url) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        print(f"Error: API returned {e.code} for {url}", file=sys.stderr)
        sys.exit(1)
    except urllib.error.URLError as e:
        print(f"Error: Could not reach API — {e.reason}", file=sys.stderr)
        sys.exit(1)


def extract_verse_text(content, include_note_ids=False):
    """Extract plain text from a verse content array. Returns (text, [noteIds])."""
    parts = []
    note_ids = []
    for item in content:
        if isinstance(item, str):
            parts.append(item)
        elif isinstance(item, dict):
            if "noteId" in item:
                note_ids.append(item["noteId"])
                if include_note_ids:
                    parts.append(f" [{item['noteId']}] ")
                else:
                    # Insert space so adjacent text fragments don't merge
                    parts.append(" ")
            elif "text" in item:
                if "poem" in item and parts:
                    parts.append("\n      ")
                parts.append(item["text"])
            else:
                parts.append(" ")
    text = re.sub(r' {2,}', ' ', "".join(parts)).strip()
    return text, note_ids


def format_verses(data, verse_start, verse_end, study=False):
    """Format verses from chapter data."""
    content = data["chapter"]["content"]
    footnotes_list = data["chapter"].get("footnotes", [])
    footnotes = {fn["noteId"]: fn for fn in footnotes_list}
    lines = []
    collected_notes = []
    in_range = verse_start is None  # If no verse specified, include everything

    for item in content:
        t = item.get("type")
        if t == "heading":
            heading_text, _ = extract_verse_text(item.get("content", []))
            if in_range or verse_start is None:
                lines.append(f"\n  [{heading_text}]\n")
        elif t == "verse":
            num = item.get("number")
            if verse_start is not None:
                if num < verse_start:
                    continue
                if num > verse_end:
                    break
                in_range = True
            text, note_ids = extract_verse_text(item.get("content", []), include_note_ids=study)
            lines.append(f"  {num}  {text}")
            if study:
                for nid in note_ids:
                    fn = footnotes.get(nid)
                    if fn:
                        collected_notes.append(fn)

    output = "\n".join(lines)
    if study and collected_notes:
        output += "\n\n  Footnotes:\n"
        for fn in collected_notes:
            ref = fn.get("reference", {})
            ref_str = f"{ref.get('chapter', '')}:{ref.get('verse', '')}" if ref else ""
            output += f"    [{fn['noteId']}] ({ref_str}) {fn.get('text', '')}\n"
    return output


def format_citation(book_id, chapter, verse_start, verse_end, translation):
    """Build a human-readable citation string."""
    name = BOOK_NAMES.get(book_id, book_id)
    cite = f"{name} {chapter}"
    if verse_start is not None:
        cite += f":{verse_start}"
        if verse_end is not None and verse_end != verse_start:
            cite += f"-{verse_end}"
    return f"{cite} ({translation})"


def fetch_cross_refs(book_id, chapter):
    """Fetch cross-references for a chapter from the open-cross-ref API."""
    url = f"https://bible.helloao.org/api/d/open-cross-ref/{book_id}/{chapter}.json"
    try:
        with urllib.request.urlopen(url) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        print(f"Warning: Cross-ref API returned {e.code} for {url}", file=sys.stderr)
        return None
    except urllib.error.URLError as e:
        print(f"Warning: Could not reach cross-ref API — {e.reason}", file=sys.stderr)
        return None


def get_cross_refs_for_verses(book_id, chapter, verse_start, verse_end):
    """Get cross-references filtered to the requested verse range."""
    data = fetch_cross_refs(book_id, chapter)
    if not data:
        return []
    content = data.get("chapter", {}).get("content", [])
    refs = []
    for item in content:
        v = item.get("verse")
        if verse_start is not None:
            if v < verse_start or v > verse_end:
                continue
        for r in item.get("references", []):
            refs.append(r)
    return refs


def format_cross_ref(ref):
    """Format a single cross-reference as a human-readable string."""
    book = BOOK_NAMES.get(ref["book"], ref["book"])
    return f"{book} {ref['chapter']}:{ref['verse']}"


def display_cross_refs(refs, expand=False, translation="BSB"):
    """Display cross-references, optionally expanding with verse text."""
    if not refs:
        print("\n  No cross-references found.")
        return

    print("\n  Cross-references:")
    if not expand:
        for r in refs:
            print(f"    - {format_cross_ref(r)}")
        return

    # Limit to first 8 references
    limited = refs[:8]

    # Group by book+chapter to minimize API calls
    grouped = {}
    for r in limited:
        key = (r["book"], r["chapter"])
        grouped.setdefault(key, []).append(r["verse"])

    # Fetch each unique chapter once, serially
    chapter_cache = {}
    for (bid, ch) in grouped:
        chapter_cache[(bid, ch)] = fetch_chapter(translation, bid, ch)

    for r in limited:
        key = (r["book"], r["chapter"])
        label = format_cross_ref(r)
        data = chapter_cache.get(key)
        if not data:
            print(f"    - {label}  [fetch error]")
            continue
        text = format_verses(data, r["verse"], r["verse"]).strip()
        # Remove the verse number prefix for cleaner inline display
        text = re.sub(r'^\d+\s*', '', text)
        print(f"    - {label}  {text}")

    if len(refs) > 8:
        print(f"    ... and {len(refs) - 8} more")


def compare_mode(book_id, chapter, verse_start, verse_end):
    """Fetch and display the same reference from multiple translations."""
    translations = ["BSB", "KJV", "ENGWEBP"]
    for tr in translations:
        data = fetch_chapter(tr, book_id, chapter)
        citation = format_citation(book_id, chapter, verse_start, verse_end, tr)
        print(f"\n--- {citation} ---")
        print(format_verses(data, verse_start, verse_end))


def main():
    parser = argparse.ArgumentParser(description="Look up Bible verses via helloao.org API")
    parser.add_argument("reference", help='Bible reference, e.g. "John 3:16" or "Psalm 23"')
    parser.add_argument("--translation", default="BSB", help="Translation ID (default: BSB)")
    parser.add_argument("--study", action="store_true", help="Include footnotes")
    parser.add_argument("--compare", action="store_true", help="Show BSB, KJV, and ENGWEBP side by side")
    parser.add_argument("--cross-refs", action="store_true", help="Show cross-references for the verse(s)")
    parser.add_argument("--expand", action="store_true", help="With --cross-refs, fetch and display referenced verse text")
    args = parser.parse_args()

    book_id, chapter, verse_start, verse_end = parse_reference(args.reference)

    if args.compare:
        compare_mode(book_id, chapter, verse_start, verse_end)
    else:
        data = fetch_chapter(args.translation, book_id, chapter)
        citation = format_citation(book_id, chapter, verse_start, verse_end, args.translation)
        print(f"\n{citation}\n")
        print(format_verses(data, verse_start, verse_end, study=args.study))

        if args.cross_refs:
            refs = get_cross_refs_for_verses(book_id, chapter, verse_start, verse_end)
            display_cross_refs(refs, expand=args.expand, translation=args.translation)


if __name__ == "__main__":
    main()
