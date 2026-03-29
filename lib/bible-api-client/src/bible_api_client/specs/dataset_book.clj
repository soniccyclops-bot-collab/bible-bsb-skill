(ns bible-api-client.specs.dataset-book
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def dataset-book-data
  {
   (ds/req :id) string?
   (ds/req :order) int?
   (ds/req :firstChapterNumber) int?
   (ds/req :firstChapterApiLink) string?
   (ds/req :lastChapterNumber) int?
   (ds/req :lastChapterApiLink) string?
   (ds/req :numberOfChapters) int?
   (ds/req :totalNumberOfVerses) int?
   (ds/req :totalNumberOfReferences) int?
   })

(def dataset-book-spec
  (ds/spec
    {:name ::dataset-book
     :spec dataset-book-data}))
