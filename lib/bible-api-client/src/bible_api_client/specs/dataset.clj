(ns bible-api-client.specs.dataset
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def dataset-data
  {
   (ds/req :id) string?
   (ds/req :name) string?
   (ds/req :website) string?
   (ds/req :licenseUrl) string?
   (ds/opt :licenseNotes) string?
   (ds/req :englishName) string?
   (ds/req :language) string?
   (ds/req :textDirection) string?
   (ds/req :listOfBooksApiLink) string?
   (ds/req :availableFormats) (s/coll-of string?)
   (ds/req :numberOfBooks) int?
   (ds/req :totalNumberOfChapters) int?
   (ds/req :totalNumberOfVerses) int?
   (ds/req :totalNumberOfReferences) int?
   (ds/opt :languageName) string?
   (ds/opt :languageEnglishName) string?
   })

(def dataset-spec
  (ds/spec
    {:name ::dataset
     :spec dataset-data}))
