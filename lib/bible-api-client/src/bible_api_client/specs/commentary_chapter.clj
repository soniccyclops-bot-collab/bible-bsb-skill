(ns bible-api-client.specs.commentary-chapter
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.chapter-verse :refer :all]
            )
  (:import (java.io File)))


(def commentary-chapter-data
  {
   (ds/req :number) int?
   (ds/opt :introduction) string?
   (ds/req :content) (s/coll-of chapter-verse-spec)
   })

(def commentary-chapter-spec
  (ds/spec
    {:name ::commentary-chapter
     :spec commentary-chapter-data}))
