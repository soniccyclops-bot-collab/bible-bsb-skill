(ns bible-api-client.specs.commentary-book
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def commentary-book-data
  {
   (ds/req :id) string?
   (ds/req :name) string?
   (ds/req :commonName) string?
   (ds/opt :introduction) string?
   (ds/opt :introductionSummary) string?
   (ds/req :order) int?
   (ds/req :firstChapterNumber) int?
   (ds/req :firstChapterApiLink) string?
   (ds/req :lastChapterNumber) int?
   (ds/req :lastChapterApiLink) string?
   (ds/req :numberOfChapters) int?
   (ds/req :totalNumberOfVerses) int?
   })

(def commentary-book-spec
  (ds/spec
    {:name ::commentary-book
     :spec commentary-book-data}))
