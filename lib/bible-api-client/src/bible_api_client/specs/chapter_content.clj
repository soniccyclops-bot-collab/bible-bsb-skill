(ns bible-api-client.specs.chapter-content
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.chapter-hebrew-subtitle-content-inner :refer :all]
            )
  (:import (java.io File)))


(def chapter-content-data
  {
   (ds/req :type) string?
   (ds/req :content) (s/coll-of chapter-hebrew-subtitle-content-inner-spec)
   (ds/req :number) int?
   })

(def chapter-content-spec
  (ds/spec
    {:name ::chapter-content
     :spec chapter-content-data}))
