(ns bible-api-client.specs.commentary-profile-content
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.commentary :refer :all]
            [bible-api-client.specs.commentary-profile-ref :refer :all]
            )
  (:import (java.io File)))


(def commentary-profile-content-data
  {
   (ds/req :commentary) commentary-spec
   (ds/req :profile) commentary-profile-ref-spec
   (ds/req :content) (s/coll-of string?)
   })

(def commentary-profile-content-spec
  (ds/spec
    {:name ::commentary-profile-content
     :spec commentary-profile-content-data}))
