(ns bible-api-client.specs.commentary-profile-reference
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def commentary-profile-reference-data
  {
   (ds/req :book) string?
   (ds/req :chapter) int?
   (ds/req :verse) int?
   })

(def commentary-profile-reference-spec
  (ds/spec
    {:name ::commentary-profile-reference
     :spec commentary-profile-reference-data}))
