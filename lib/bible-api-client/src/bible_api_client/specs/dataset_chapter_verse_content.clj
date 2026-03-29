(ns bible-api-client.specs.dataset-chapter-verse-content
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.cross-reference :refer :all]
            )
  (:import (java.io File)))


(def dataset-chapter-verse-content-data
  {
   (ds/req :verse) int?
   (ds/req :references) (s/coll-of cross-reference-spec)
   })

(def dataset-chapter-verse-content-spec
  (ds/spec
    {:name ::dataset-chapter-verse-content
     :spec dataset-chapter-verse-content-data}))
