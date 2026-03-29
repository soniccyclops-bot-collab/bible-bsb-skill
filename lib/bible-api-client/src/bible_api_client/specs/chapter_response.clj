(ns bible-api-client.specs.chapter-response
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.chapter :refer :all]
            [bible-api-client.specs.translation :refer :all]
            [bible-api-client.specs.translation-book :refer :all]
            )
  (:import (java.io File)))


(def chapter-response-data
  {
   (ds/req :chapter) chapter-spec
   (ds/req :thisChapterAudioLinks) (s/map-of string? string?)
   (ds/req :translation) translation-spec
   (ds/req :book) translation-book-spec
   (ds/req :thisChapterLink) string?
   (ds/req :nextChapterApiLink) string?
   (ds/req :nextChapterAudioLinks) (s/map-of string? string?)
   (ds/req :previousChapterApiLink) string?
   (ds/req :previousChapterAudioLinks) (s/map-of string? string?)
   (ds/req :numberOfVerses) int?
   })

(def chapter-response-spec
  (ds/spec
    {:name ::chapter-response
     :spec chapter-response-data}))
