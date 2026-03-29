(ns bible-api-client.specs.verse-footnote-reference
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def verse-footnote-reference-data
  {
   (ds/req :noteId) int?
   })

(def verse-footnote-reference-spec
  (ds/spec
    {:name ::verse-footnote-reference
     :spec verse-footnote-reference-data}))
