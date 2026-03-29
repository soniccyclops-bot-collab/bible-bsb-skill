(ns bible-api-client.specs.chapter
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.chapter-content :refer :all]
            [bible-api-client.specs.chapter-footnote :refer :all]
            )
  (:import (java.io File)))


(def chapter-data
  {
   (ds/req :number) int?
   (ds/req :content) (s/coll-of chapter-content-spec)
   (ds/req :footnotes) (s/coll-of chapter-footnote-spec)
   })

(def chapter-spec
  (ds/spec
    {:name ::chapter
     :spec chapter-data}))
