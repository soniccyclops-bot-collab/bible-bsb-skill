(ns bible-api-client.specs.chapter-line-break
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def chapter-line-break-data
  {
   (ds/req :type) string?
   })

(def chapter-line-break-spec
  (ds/spec
    {:name ::chapter-line-break
     :spec chapter-line-break-data}))
