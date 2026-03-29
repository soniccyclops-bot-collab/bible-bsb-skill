(ns bible-api-client.api.default
  (:require [bible-api-client.core :refer [call-api check-required-params with-collection-format *api-context*]]
            [clojure.spec.alpha :as s]
            [spec-tools.core :as st]
            [orchestra.core :refer [defn-spec]]
            [bible-api-client.specs.inline-heading :refer :all]
            [bible-api-client.specs.commentary-profile-reference :refer :all]
            [bible-api-client.specs.chapter :refer :all]
            [bible-api-client.specs.dataset-book-chapter :refer :all]
            [bible-api-client.specs.chapter-verse :refer :all]
            [bible-api-client.specs.cross-reference :refer :all]
            [bible-api-client.specs.chapter-footnote-caller :refer :all]
            [bible-api-client.specs.dataset-book :refer :all]
            [bible-api-client.specs.translation-complete-book :refer :all]
            [bible-api-client.specs.chapter-content :refer :all]
            [bible-api-client.specs.commentary-profile-content :refer :all]
            [bible-api-client.specs.chapter-footnote :refer :all]
            [bible-api-client.specs.translation-book :refer :all]
            [bible-api-client.specs.translation-complete :refer :all]
            [bible-api-client.specs.verse-footnote-reference :refer :all]
            [bible-api-client.specs.commentary-chapter :refer :all]
            [bible-api-client.specs.dataset-chapter-data :refer :all]
            [bible-api-client.specs.chapter-response :refer :all]
            [bible-api-client.specs.list-commentaries-200-response :refer :all]
            [bible-api-client.specs.inline-line-break :refer :all]
            [bible-api-client.specs.list-datasets-200-response :refer :all]
            [bible-api-client.specs.chapter-hebrew-subtitle-content-inner :refer :all]
            [bible-api-client.specs.chapter-line-break :refer :all]
            [bible-api-client.specs.verse-content-item :refer :all]
            [bible-api-client.specs.chapter-heading :refer :all]
            [bible-api-client.specs.list-commentary-profiles-200-response :refer :all]
            [bible-api-client.specs.commentary-book-chapter :refer :all]
            [bible-api-client.specs.commentary-profile-ref-reference :refer :all]
            [bible-api-client.specs.list-commentary-books-200-response :refer :all]
            [bible-api-client.specs.chapter-footnote-reference :refer :all]
            [bible-api-client.specs.chapter-hebrew-subtitle :refer :all]
            [bible-api-client.specs.formatted-text :refer :all]
            [bible-api-client.specs.dataset-chapter-verse-content :refer :all]
            [bible-api-client.specs.list-books-200-response :refer :all]
            [bible-api-client.specs.commentary-book :refer :all]
            [bible-api-client.specs.translation :refer :all]
            [bible-api-client.specs.commentary-profile :refer :all]
            [bible-api-client.specs.list-translations-200-response :refer :all]
            [bible-api-client.specs.commentary-profile-ref :refer :all]
            [bible-api-client.specs.dataset :refer :all]
            [bible-api-client.specs.commentary :refer :all]
            [bible-api-client.specs.list-dataset-books-200-response :refer :all]
            )
  (:import (java.io File)))


(defn-spec get-chapter-with-http-info any?
  "Get chapter content"
  [translationId string?, bookId string?, chapter int?]
  (check-required-params translationId bookId chapter)
  (call-api "/api/{translationId}/{bookId}/{chapter}.json" :get
            {:path-params   {"translationId" translationId "bookId" bookId "chapter" chapter }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec get-chapter chapter-response-spec
  "Get chapter content"
  [translationId string?, bookId string?, chapter int?]
  (let [res (:data (get-chapter-with-http-info translationId bookId chapter))]
    (if (:decode-models *api-context*)
       (st/decode chapter-response-spec res st/string-transformer)
       res)))


(defn-spec get-commentary-chapter-with-http-info any?
  "Get commentary chapter content"
  [commentaryId string?, bookId string?, chapter int?]
  (check-required-params commentaryId bookId chapter)
  (call-api "/api/c/{commentaryId}/{bookId}/{chapter}.json" :get
            {:path-params   {"commentaryId" commentaryId "bookId" bookId "chapter" chapter }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec get-commentary-chapter commentary-book-chapter-spec
  "Get commentary chapter content"
  [commentaryId string?, bookId string?, chapter int?]
  (let [res (:data (get-commentary-chapter-with-http-info commentaryId bookId chapter))]
    (if (:decode-models *api-context*)
       (st/decode commentary-book-chapter-spec res st/string-transformer)
       res)))


(defn-spec get-commentary-profile-with-http-info any?
  "Get commentary profile content"
  [commentaryId string?, profileId string?]
  (check-required-params commentaryId profileId)
  (call-api "/api/c/{commentaryId}/profiles/{profileId}.json" :get
            {:path-params   {"commentaryId" commentaryId "profileId" profileId }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec get-commentary-profile commentary-profile-content-spec
  "Get commentary profile content"
  [commentaryId string?, profileId string?]
  (let [res (:data (get-commentary-profile-with-http-info commentaryId profileId))]
    (if (:decode-models *api-context*)
       (st/decode commentary-profile-content-spec res st/string-transformer)
       res)))


(defn-spec get-complete-translation-with-http-info any?
  "Get complete translation download"
  [translationId string?]
  (check-required-params translationId)
  (call-api "/api/{translationId}/complete.json" :get
            {:path-params   {"translationId" translationId }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec get-complete-translation translation-complete-spec
  "Get complete translation download"
  [translationId string?]
  (let [res (:data (get-complete-translation-with-http-info translationId))]
    (if (:decode-models *api-context*)
       (st/decode translation-complete-spec res st/string-transformer)
       res)))


(defn-spec get-dataset-chapter-with-http-info any?
  "Get dataset chapter content (cross-references)"
  [datasetId string?, bookId string?, chapter int?]
  (check-required-params datasetId bookId chapter)
  (call-api "/api/d/{datasetId}/{bookId}/{chapter}.json" :get
            {:path-params   {"datasetId" datasetId "bookId" bookId "chapter" chapter }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec get-dataset-chapter dataset-book-chapter-spec
  "Get dataset chapter content (cross-references)"
  [datasetId string?, bookId string?, chapter int?]
  (let [res (:data (get-dataset-chapter-with-http-info datasetId bookId chapter))]
    (if (:decode-models *api-context*)
       (st/decode dataset-book-chapter-spec res st/string-transformer)
       res)))


(defn-spec list-books-with-http-info any?
  "List books for a translation"
  [translationId string?]
  (check-required-params translationId)
  (call-api "/api/{translationId}/books.json" :get
            {:path-params   {"translationId" translationId }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec list-books list-books-200-response-spec
  "List books for a translation"
  [translationId string?]
  (let [res (:data (list-books-with-http-info translationId))]
    (if (:decode-models *api-context*)
       (st/decode list-books-200-response-spec res st/string-transformer)
       res)))


(defn-spec list-commentaries-with-http-info any?
  "List all available commentaries"
  []
  (call-api "/api/available_commentaries.json" :get
            {:path-params   {}
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec list-commentaries list-commentaries-200-response-spec
  "List all available commentaries"
  []
  (let [res (:data (list-commentaries-with-http-info))]
    (if (:decode-models *api-context*)
       (st/decode list-commentaries-200-response-spec res st/string-transformer)
       res)))


(defn-spec list-commentary-books-with-http-info any?
  "List books for a commentary"
  [commentaryId string?]
  (check-required-params commentaryId)
  (call-api "/api/c/{commentaryId}/books.json" :get
            {:path-params   {"commentaryId" commentaryId }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec list-commentary-books list-commentary-books-200-response-spec
  "List books for a commentary"
  [commentaryId string?]
  (let [res (:data (list-commentary-books-with-http-info commentaryId))]
    (if (:decode-models *api-context*)
       (st/decode list-commentary-books-200-response-spec res st/string-transformer)
       res)))


(defn-spec list-commentary-profiles-with-http-info any?
  "List profiles for a commentary"
  [commentaryId string?]
  (check-required-params commentaryId)
  (call-api "/api/c/{commentaryId}/profiles.json" :get
            {:path-params   {"commentaryId" commentaryId }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec list-commentary-profiles list-commentary-profiles-200-response-spec
  "List profiles for a commentary"
  [commentaryId string?]
  (let [res (:data (list-commentary-profiles-with-http-info commentaryId))]
    (if (:decode-models *api-context*)
       (st/decode list-commentary-profiles-200-response-spec res st/string-transformer)
       res)))


(defn-spec list-dataset-books-with-http-info any?
  "List books for a dataset"
  [datasetId string?]
  (check-required-params datasetId)
  (call-api "/api/d/{datasetId}/books.json" :get
            {:path-params   {"datasetId" datasetId }
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec list-dataset-books list-dataset-books-200-response-spec
  "List books for a dataset"
  [datasetId string?]
  (let [res (:data (list-dataset-books-with-http-info datasetId))]
    (if (:decode-models *api-context*)
       (st/decode list-dataset-books-200-response-spec res st/string-transformer)
       res)))


(defn-spec list-datasets-with-http-info any?
  "List all available datasets"
  []
  (call-api "/api/available_datasets.json" :get
            {:path-params   {}
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec list-datasets list-datasets-200-response-spec
  "List all available datasets"
  []
  (let [res (:data (list-datasets-with-http-info))]
    (if (:decode-models *api-context*)
       (st/decode list-datasets-200-response-spec res st/string-transformer)
       res)))


(defn-spec list-translations-with-http-info any?
  "List all available translations"
  []
  (call-api "/api/available_translations.json" :get
            {:path-params   {}
             :header-params {}
             :query-params  {}
             :form-params   {}
             :content-types []
             :accepts       ["application/json"]
             :auth-names    []}))

(defn-spec list-translations list-translations-200-response-spec
  "List all available translations"
  []
  (let [res (:data (list-translations-with-http-info))]
    (if (:decode-models *api-context*)
       (st/decode list-translations-200-response-spec res st/string-transformer)
       res)))


