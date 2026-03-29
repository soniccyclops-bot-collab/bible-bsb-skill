(ns bible-api-client.specs.chapter-hebrew-subtitle
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.chapter-hebrew-subtitle-content-inner :refer :all]
            )
  (:import (java.io File)))


(def chapter-hebrew-subtitle-data
  {
   (ds/req :type) string?
   (ds/req :content) (s/coll-of chapter-hebrew-subtitle-content-inner-spec)
   })

(def chapter-hebrew-subtitle-spec
  (ds/spec
    {:name ::chapter-hebrew-subtitle
     :spec chapter-hebrew-subtitle-data}))
