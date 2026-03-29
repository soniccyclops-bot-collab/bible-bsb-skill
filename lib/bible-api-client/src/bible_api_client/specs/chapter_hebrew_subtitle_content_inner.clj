(ns bible-api-client.specs.chapter-hebrew-subtitle-content-inner
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def chapter-hebrew-subtitle-content-inner-data
  {
   (ds/req :text) string?
   (ds/opt :poem) int?
   (ds/opt :wordsOfJesus) boolean?
   (ds/req :noteId) int?
   })

(def chapter-hebrew-subtitle-content-inner-spec
  (ds/spec
    {:name ::chapter-hebrew-subtitle-content-inner
     :spec chapter-hebrew-subtitle-content-inner-data}))
