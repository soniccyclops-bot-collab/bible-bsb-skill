(ns bible-api-client.specs.inline-line-break
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def inline-line-break-data
  {
   (ds/req :lineBreak) boolean?
   })

(def inline-line-break-spec
  (ds/spec
    {:name ::inline-line-break
     :spec inline-line-break-data}))
