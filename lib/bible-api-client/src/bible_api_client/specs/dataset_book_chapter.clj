(ns bible-api-client.specs.dataset-book-chapter
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.dataset-chapter-data :refer :all]
            [bible-api-client.specs.dataset :refer :all]
            [bible-api-client.specs.dataset-book :refer :all]
            )
  (:import (java.io File)))


(def dataset-book-chapter-data
  {
   (ds/req :chapter) dataset-chapter-data-spec
   (ds/req :dataset) dataset-spec
   (ds/req :book) dataset-book-spec
   (ds/req :thisChapterLink) string?
   (ds/req :nextChapterApiLink) string?
   (ds/req :previousChapterApiLink) string?
   (ds/req :numberOfVerses) int?
   (ds/req :numberOfReferences) int?
   })

(def dataset-book-chapter-spec
  (ds/spec
    {:name ::dataset-book-chapter
     :spec dataset-book-chapter-data}))
