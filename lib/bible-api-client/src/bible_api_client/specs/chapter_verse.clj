(ns bible-api-client.specs.chapter-verse
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.verse-content-item :refer :all]
            )
  (:import (java.io File)))


(def chapter-verse-data
  {
   (ds/req :type) string?
   (ds/req :number) int?
   (ds/req :content) (s/coll-of verse-content-item-spec)
   })

(def chapter-verse-spec
  (ds/spec
    {:name ::chapter-verse
     :spec chapter-verse-data}))
