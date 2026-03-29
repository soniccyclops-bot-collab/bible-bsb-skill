(ns bible-api-client.specs.dataset-chapter-data
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.dataset-chapter-verse-content :refer :all]
            )
  (:import (java.io File)))


(def dataset-chapter-data-data
  {
   (ds/req :number) int?
   (ds/req :content) (s/coll-of dataset-chapter-verse-content-spec)
   })

(def dataset-chapter-data-spec
  (ds/spec
    {:name ::dataset-chapter-data
     :spec dataset-chapter-data-data}))
