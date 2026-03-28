"""Auto-generated API client from OpenAPI spec."""
from __future__ import annotations

import json
import urllib.request
import urllib.error
from typing import Any, Dict, List, Optional

from .models import *


class APIError(Exception):
    """Raised when an API request fails."""

    def __init__(self, message: str, status_code: Optional[int] = None):
        super().__init__(message)
        self.status_code = status_code


def _from_dict(cls, data):
    """Recursively construct a dataclass from a dict."""
    if data is None or not isinstance(data, dict):
        return data
    import dataclasses
    if not dataclasses.is_dataclass(cls):
        return data
    field_map = {}
    for f in dataclasses.fields(cls):
        # Check the comment for the original json key name
        field_map[f.name] = f
    kwargs = {}
    # Build reverse map: json_key -> field
    json_to_field = {}
    for f in dataclasses.fields(cls):
        if f.metadata and "json_key" in f.metadata:
            json_to_field[f.metadata["json_key"]] = f
        json_to_field[f.name] = f
    for key, val in data.items():
        snake = key
        # Try camelCase -> snake_case
        import re
        s1 = re.sub("(.)([A-Z][a-z]+)", r"\1_\2", key)
        snake = re.sub("([a-z0-9])([A-Z])", r"\1_\2", s1).lower()
        if snake in field_map:
            kwargs[snake] = val
        elif key in field_map:
            kwargs[key] = val
    return cls(**kwargs)


class BibleAPIClient:
    """Auto-generated client for the helloao.org Bible API."""

    DEFAULT_BASE_URL = "https://bible.helloao.org"

    def __init__(self, base_url: Optional[str] = None, timeout: int = 30):
        self.base_url = (base_url or self.DEFAULT_BASE_URL).rstrip("/")
        self.timeout = timeout

    def _request(self, path: str) -> Any:
        """Make a GET request and return parsed JSON."""
        url = self.base_url + path
        req = urllib.request.Request(url, headers={"Accept": "application/json"})
        try:
            with urllib.request.urlopen(req, timeout=self.timeout) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            raise APIError(f"HTTP {e.code}: {e.reason}", e.code) from e
        except urllib.error.URLError as e:
            raise APIError(f"Connection error: {e.reason}") from e

    def list_translations(self) -> Dict[str, Any]:
        """List all available translations"""
        path = "/api/available_translations.json"
        return self._request(path)

    def list_books(self, translation_id: str) -> Dict[str, Any]:
        """List books for a translation"""
        path = f"/api/{translation_id}/books.json"
        return self._request(path)

    def get_chapter(self, translation_id: str, book_id: str, chapter: int) -> ChapterResponse:
        """Get chapter content"""
        path = f"/api/{translation_id}/{book_id}/{chapter}.json"
        return _from_dict(ChapterResponse, self._request(path))

    def get_complete_translation(self, translation_id: str) -> TranslationComplete:
        """Get complete translation download"""
        path = f"/api/{translation_id}/complete.json"
        return _from_dict(TranslationComplete, self._request(path))

    def list_commentaries(self) -> Dict[str, Any]:
        """List all available commentaries"""
        path = "/api/available_commentaries.json"
        return self._request(path)

    def list_commentary_books(self, commentary_id: str) -> Dict[str, Any]:
        """List books for a commentary"""
        path = f"/api/c/{commentary_id}/books.json"
        return self._request(path)

    def get_commentary_chapter(self, commentary_id: str, book_id: str, chapter: int) -> CommentaryBookChapter:
        """Get commentary chapter content"""
        path = f"/api/c/{commentary_id}/{book_id}/{chapter}.json"
        return _from_dict(CommentaryBookChapter, self._request(path))

    def list_commentary_profiles(self, commentary_id: str) -> Dict[str, Any]:
        """List profiles for a commentary"""
        path = f"/api/c/{commentary_id}/profiles.json"
        return self._request(path)

    def get_commentary_profile(self, commentary_id: str, profile_id: str) -> CommentaryProfileContent:
        """Get commentary profile content"""
        path = f"/api/c/{commentary_id}/profiles/{profile_id}.json"
        return _from_dict(CommentaryProfileContent, self._request(path))

    def list_datasets(self) -> Dict[str, Any]:
        """List all available datasets"""
        path = "/api/available_datasets.json"
        return self._request(path)

    def list_dataset_books(self, dataset_id: str) -> Dict[str, Any]:
        """List books for a dataset"""
        path = f"/api/d/{dataset_id}/books.json"
        return self._request(path)

    def get_dataset_chapter(self, dataset_id: str, book_id: str, chapter: int) -> DatasetBookChapter:
        """Get dataset chapter content (cross-references)"""
        path = f"/api/d/{dataset_id}/{book_id}/{chapter}.json"
        return _from_dict(DatasetBookChapter, self._request(path))
