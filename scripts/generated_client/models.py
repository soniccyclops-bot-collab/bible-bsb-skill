"""Auto-generated dataclass models from OpenAPI spec."""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional, Union


@dataclass
class Translation:
    id: str
    name: str
    website: str
    license_url: str  # json: licenseUrl
    english_name: str  # json: englishName
    language: str
    text_direction: str  # json: textDirection
    list_of_books_api_link: str  # json: listOfBooksApiLink
    available_formats: List[str]  # json: availableFormats
    number_of_books: int  # json: numberOfBooks
    total_number_of_chapters: int  # json: totalNumberOfChapters
    total_number_of_verses: int  # json: totalNumberOfVerses
    license_notes: Optional[str] = None  # json: licenseNotes
    license_notice: Optional[str] = None  # json: licenseNotice
    short_name: Optional[str] = None  # json: shortName
    number_of_apocryphal_books: Optional[int] = None  # json: numberOfApocryphalBooks
    total_number_of_apocryphal_chapters: Optional[int] = None  # json: totalNumberOfApocryphalChapters
    total_number_of_apocryphal_verses: Optional[int] = None  # json: totalNumberOfApocryphalVerses
    language_name: Optional[str] = None  # json: languageName
    language_english_name: Optional[str] = None  # json: languageEnglishName
    complete_translation_api_link: Optional[str] = None  # json: completeTranslationApiLink


@dataclass
class TranslationBook:
    id: str
    name: str
    common_name: str  # json: commonName
    title: Optional[str]
    order: int
    first_chapter_number: int  # json: firstChapterNumber
    first_chapter_api_link: str  # json: firstChapterApiLink
    last_chapter_number: int  # json: lastChapterNumber
    last_chapter_api_link: str  # json: lastChapterApiLink
    number_of_chapters: int  # json: numberOfChapters
    total_number_of_verses: int  # json: totalNumberOfVerses
    is_apocryphal: Optional[bool] = None  # json: isApocryphal


@dataclass
class ChapterResponse:
    chapter: Chapter
    this_chapter_audio_links: AudioLinks  # json: thisChapterAudioLinks
    translation: Translation
    book: TranslationBook
    this_chapter_link: str  # json: thisChapterLink
    next_chapter_api_link: Optional[str]  # json: nextChapterApiLink
    next_chapter_audio_links: Optional[AudioLinks]  # json: nextChapterAudioLinks
    previous_chapter_api_link: Optional[str]  # json: previousChapterApiLink
    previous_chapter_audio_links: Optional[AudioLinks]  # json: previousChapterAudioLinks
    number_of_verses: int  # json: numberOfVerses


@dataclass
class TranslationComplete:
    translation: Translation
    books: List[TranslationCompleteBook]


@dataclass
class TranslationCompleteBook:
    id: str
    name: str
    common_name: str  # json: commonName
    title: Optional[str]
    order: int
    number_of_chapters: int  # json: numberOfChapters
    total_number_of_verses: int  # json: totalNumberOfVerses
    chapters: List[TranslationBookChapter]
    is_apocryphal: Optional[bool] = None  # json: isApocryphal


@dataclass
class Commentary:
    id: str
    name: str
    website: str
    license_url: str  # json: licenseUrl
    english_name: str  # json: englishName
    language: str
    text_direction: str  # json: textDirection
    list_of_books_api_link: str  # json: listOfBooksApiLink
    list_of_profiles_api_link: str  # json: listOfProfilesApiLink
    available_formats: List[str]  # json: availableFormats
    number_of_books: int  # json: numberOfBooks
    total_number_of_chapters: int  # json: totalNumberOfChapters
    total_number_of_verses: int  # json: totalNumberOfVerses
    total_number_of_profiles: int  # json: totalNumberOfProfiles
    license_notes: Optional[str] = None  # json: licenseNotes
    language_name: Optional[str] = None  # json: languageName
    language_english_name: Optional[str] = None  # json: languageEnglishName


@dataclass
class CommentaryBook:
    id: str
    name: str
    common_name: str  # json: commonName
    order: int
    first_chapter_number: Optional[int]  # json: firstChapterNumber
    first_chapter_api_link: Optional[str]  # json: firstChapterApiLink
    last_chapter_number: Optional[int]  # json: lastChapterNumber
    last_chapter_api_link: Optional[str]  # json: lastChapterApiLink
    number_of_chapters: int  # json: numberOfChapters
    total_number_of_verses: int  # json: totalNumberOfVerses
    introduction: Optional[str] = None
    introduction_summary: Optional[str] = None  # json: introductionSummary


@dataclass
class CommentaryBookChapter:
    chapter: CommentaryChapter
    commentary: Commentary
    book: CommentaryBook
    this_chapter_link: str  # json: thisChapterLink
    next_chapter_api_link: Optional[str]  # json: nextChapterApiLink
    previous_chapter_api_link: Optional[str]  # json: previousChapterApiLink
    number_of_verses: int  # json: numberOfVerses


@dataclass
class CommentaryProfileRef:
    id: str
    subject: str
    this_profile_link: str  # json: thisProfileLink
    reference: Optional[Dict[str, Any]] = None
    reference_chapter_link: Optional[str] = None  # json: referenceChapterLink


@dataclass
class CommentaryProfileContent:
    commentary: Commentary
    profile: CommentaryProfileRef
    content: List[str]


@dataclass
class Dataset:
    id: str
    name: str
    website: str
    license_url: str  # json: licenseUrl
    english_name: str  # json: englishName
    language: str
    text_direction: str  # json: textDirection
    list_of_books_api_link: str  # json: listOfBooksApiLink
    available_formats: List[str]  # json: availableFormats
    number_of_books: int  # json: numberOfBooks
    total_number_of_chapters: int  # json: totalNumberOfChapters
    total_number_of_verses: int  # json: totalNumberOfVerses
    total_number_of_references: int  # json: totalNumberOfReferences
    license_notes: Optional[str] = None  # json: licenseNotes
    language_name: Optional[str] = None  # json: languageName
    language_english_name: Optional[str] = None  # json: languageEnglishName


@dataclass
class DatasetBook:
    id: str
    order: int
    first_chapter_number: int  # json: firstChapterNumber
    first_chapter_api_link: str  # json: firstChapterApiLink
    last_chapter_number: int  # json: lastChapterNumber
    last_chapter_api_link: str  # json: lastChapterApiLink
    number_of_chapters: int  # json: numberOfChapters
    total_number_of_verses: int  # json: totalNumberOfVerses
    total_number_of_references: int  # json: totalNumberOfReferences


@dataclass
class DatasetBookChapter:
    chapter: DatasetChapterData
    dataset: Dataset
    book: DatasetBook
    this_chapter_link: str  # json: thisChapterLink
    next_chapter_api_link: Optional[str]  # json: nextChapterApiLink
    previous_chapter_api_link: Optional[str]  # json: previousChapterApiLink
    number_of_verses: int  # json: numberOfVerses
    number_of_references: int  # json: numberOfReferences


@dataclass
class Chapter:
    number: int
    content: List[ChapterContent]
    footnotes: List[ChapterFootnote]


@dataclass
class CommentaryChapter:
    number: int
    content: List[ChapterVerse]
    introduction: Optional[str] = None


@dataclass
class ChapterHeading:
    type: str
    content: List[str]


@dataclass
class ChapterLineBreak:
    type: str


@dataclass
class ChapterVerse:
    type: str
    number: int
    content: List[VerseContentItem]


@dataclass
class ChapterHebrewSubtitle:
    type: str
    content: List[Union[str, FormattedText, VerseFootnoteReference]]


@dataclass
class FormattedText:
    text: str
    poem: Optional[int] = None
    words_of_jesus: Optional[bool] = None  # json: wordsOfJesus


@dataclass
class InlineHeading:
    heading: str


@dataclass
class InlineLineBreak:
    line_break: bool  # json: lineBreak


@dataclass
class VerseFootnoteReference:
    note_id: int  # json: noteId


@dataclass
class ChapterFootnote:
    note_id: int  # json: noteId
    text: str
    caller: Union[str, str]
    reference: Optional[Dict[str, Any]] = None


@dataclass
class DatasetChapterData:
    number: int
    content: List[DatasetChapterVerseContent]


@dataclass
class DatasetChapterVerseContent:
    verse: int
    references: List[CrossReference]


@dataclass
class CrossReference:
    book: str
    chapter: int
    verse: int
    score: float


@dataclass
class CommentaryProfile:
    id: str
    subject: str
    reference: Optional[Dict[str, Any]]



# Type aliases (must be after class definitions)
ChapterContent = Union[ChapterHeading, ChapterLineBreak, ChapterVerse, ChapterHebrewSubtitle]
AudioLinks = Dict[str, str]
VerseContentItem = Union[str, FormattedText, VerseFootnoteReference, InlineHeading, InlineLineBreak]
