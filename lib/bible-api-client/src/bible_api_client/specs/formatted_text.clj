(ns bible-api-client.specs.formatted-text
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def formatted-text-data
  {
   (ds/req :text) string?
   (ds/opt :poem) int?
   (ds/opt :wordsOfJesus) boolean?
   })

(def formatted-text-spec
  (ds/spec
    {:name ::formatted-text
     :spec formatted-text-data}))
