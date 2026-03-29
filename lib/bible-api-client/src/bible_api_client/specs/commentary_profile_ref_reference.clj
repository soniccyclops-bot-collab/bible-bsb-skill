(ns bible-api-client.specs.commentary-profile-ref-reference
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def commentary-profile-ref-reference-data
  {
   (ds/opt :book) string?
   (ds/opt :chapter) int?
   (ds/opt :verse) int?
   })

(def commentary-profile-ref-reference-spec
  (ds/spec
    {:name ::commentary-profile-ref-reference
     :spec commentary-profile-ref-reference-data}))
