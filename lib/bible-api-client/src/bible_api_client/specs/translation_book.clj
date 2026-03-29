(ns bible-api-client.specs.translation-book
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def translation-book-data
  {
   (ds/req :id) string?
   (ds/req :name) string?
   (ds/req :commonName) string?
   (ds/req :title) string?
   (ds/req :order) int?
   (ds/opt :isApocryphal) boolean?
   (ds/req :firstChapterNumber) int?
   (ds/req :firstChapterApiLink) string?
   (ds/req :lastChapterNumber) int?
   (ds/req :lastChapterApiLink) string?
   (ds/req :numberOfChapters) int?
   (ds/req :totalNumberOfVerses) int?
   })

(def translation-book-spec
  (ds/spec
    {:name ::translation-book
     :spec translation-book-data}))
