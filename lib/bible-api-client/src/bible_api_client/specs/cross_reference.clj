(ns bible-api-client.specs.cross-reference
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def cross-reference-data
  {
   (ds/req :book) string?
   (ds/req :chapter) int?
   (ds/req :verse) int?
   (ds/req :score) float?
   })

(def cross-reference-spec
  (ds/spec
    {:name ::cross-reference
     :spec cross-reference-data}))
