(ns bible-api-client.specs.translation-complete-book
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.chapter-response :refer :all]
            )
  (:import (java.io File)))


(def translation-complete-book-data
  {
   (ds/req :id) string?
   (ds/req :name) string?
   (ds/req :commonName) string?
   (ds/req :title) string?
   (ds/req :order) int?
   (ds/req :numberOfChapters) int?
   (ds/req :totalNumberOfVerses) int?
   (ds/opt :isApocryphal) boolean?
   (ds/req :chapters) (s/coll-of chapter-response-spec)
   })

(def translation-complete-book-spec
  (ds/spec
    {:name ::translation-complete-book
     :spec translation-complete-book-data}))
