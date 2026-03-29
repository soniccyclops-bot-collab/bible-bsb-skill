(ns bible-api-client.specs.verse-content-item
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def verse-content-item-data
  {
   (ds/req :text) string?
   (ds/opt :poem) int?
   (ds/opt :wordsOfJesus) boolean?
   (ds/req :noteId) int?
   (ds/req :heading) string?
   (ds/req :lineBreak) boolean?
   })

(def verse-content-item-spec
  (ds/spec
    {:name ::verse-content-item
     :spec verse-content-item-data}))
