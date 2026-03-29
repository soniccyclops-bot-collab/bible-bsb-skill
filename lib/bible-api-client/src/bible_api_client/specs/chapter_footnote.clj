(ns bible-api-client.specs.chapter-footnote
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.chapter-footnote-reference :refer :all]
            [bible-api-client.specs.chapter-footnote-caller :refer :all]
            )
  (:import (java.io File)))


(def chapter-footnote-data
  {
   (ds/req :noteId) int?
   (ds/req :text) string?
   (ds/opt :reference) chapter-footnote-reference-spec
   (ds/req :caller) chapter-footnote-caller-spec
   })

(def chapter-footnote-spec
  (ds/spec
    {:name ::chapter-footnote
     :spec chapter-footnote-data}))
