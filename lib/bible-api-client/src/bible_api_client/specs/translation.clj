(ns bible-api-client.specs.translation
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def translation-data
  {
   (ds/req :id) string?
   (ds/req :name) string?
   (ds/req :website) string?
   (ds/req :licenseUrl) string?
   (ds/opt :licenseNotes) string?
   (ds/opt :licenseNotice) string?
   (ds/opt :shortName) string?
   (ds/req :englishName) string?
   (ds/req :language) string?
   (ds/req :textDirection) string?
   (ds/req :listOfBooksApiLink) string?
   (ds/req :availableFormats) (s/coll-of string?)
   (ds/req :numberOfBooks) int?
   (ds/req :totalNumberOfChapters) int?
   (ds/req :totalNumberOfVerses) int?
   (ds/opt :numberOfApocryphalBooks) int?
   (ds/opt :totalNumberOfApocryphalChapters) int?
   (ds/opt :totalNumberOfApocryphalVerses) int?
   (ds/opt :languageName) string?
   (ds/opt :languageEnglishName) string?
   (ds/opt :completeTranslationApiLink) string?
   })

(def translation-spec
  (ds/spec
    {:name ::translation
     :spec translation-data}))
