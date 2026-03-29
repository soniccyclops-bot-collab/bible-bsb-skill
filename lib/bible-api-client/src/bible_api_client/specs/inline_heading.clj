(ns bible-api-client.specs.inline-heading
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def inline-heading-data
  {
   (ds/req :heading) string?
   })

(def inline-heading-spec
  (ds/spec
    {:name ::inline-heading
     :spec inline-heading-data}))
