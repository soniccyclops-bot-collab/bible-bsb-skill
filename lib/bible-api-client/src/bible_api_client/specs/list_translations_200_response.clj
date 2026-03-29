(ns bible-api-client.specs.list-translations-200-response
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.translation :refer :all]
            )
  (:import (java.io File)))


(def list-translations-200-response-data
  {
   (ds/req :translations) (s/coll-of translation-spec)
   })

(def list-translations-200-response-spec
  (ds/spec
    {:name ::list-translations-200-response
     :spec list-translations-200-response-data}))
