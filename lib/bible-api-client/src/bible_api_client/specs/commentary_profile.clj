(ns bible-api-client.specs.commentary-profile
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.commentary-profile-reference :refer :all]
            )
  (:import (java.io File)))


(def commentary-profile-data
  {
   (ds/req :id) string?
   (ds/req :subject) string?
   (ds/req :reference) commentary-profile-reference-spec
   })

(def commentary-profile-spec
  (ds/spec
    {:name ::commentary-profile
     :spec commentary-profile-data}))
