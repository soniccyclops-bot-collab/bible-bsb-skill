(ns bible-api-client.specs.list-commentary-profiles-200-response
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.commentary :refer :all]
            [bible-api-client.specs.commentary-profile-ref :refer :all]
            )
  (:import (java.io File)))


(def list-commentary-profiles-200-response-data
  {
   (ds/req :commentary) commentary-spec
   (ds/req :profiles) (s/coll-of commentary-profile-ref-spec)
   })

(def list-commentary-profiles-200-response-spec
  (ds/spec
    {:name ::list-commentary-profiles-200-response
     :spec list-commentary-profiles-200-response-data}))
