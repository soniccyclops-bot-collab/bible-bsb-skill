(ns bible-api-client.specs.list-datasets-200-response
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.dataset :refer :all]
            )
  (:import (java.io File)))


(def list-datasets-200-response-data
  {
   (ds/req :datasets) (s/coll-of dataset-spec)
   })

(def list-datasets-200-response-spec
  (ds/spec
    {:name ::list-datasets-200-response
     :spec list-datasets-200-response-data}))
