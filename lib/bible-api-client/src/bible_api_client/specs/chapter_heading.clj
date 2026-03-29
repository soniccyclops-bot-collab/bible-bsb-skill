(ns bible-api-client.specs.chapter-heading
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def chapter-heading-data
  {
   (ds/req :type) string?
   (ds/req :content) (s/coll-of string?)
   })

(def chapter-heading-spec
  (ds/spec
    {:name ::chapter-heading
     :spec chapter-heading-data}))
