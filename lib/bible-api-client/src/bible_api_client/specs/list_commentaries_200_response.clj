(ns bible-api-client.specs.list-commentaries-200-response
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.commentary :refer :all]
            )
  (:import (java.io File)))


(def list-commentaries-200-response-data
  {
   (ds/req :commentaries) (s/coll-of commentary-spec)
   })

(def list-commentaries-200-response-spec
  (ds/spec
    {:name ::list-commentaries-200-response
     :spec list-commentaries-200-response-data}))
