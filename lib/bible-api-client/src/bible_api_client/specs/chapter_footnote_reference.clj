(ns bible-api-client.specs.chapter-footnote-reference
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def chapter-footnote-reference-data
  {
   (ds/req :chapter) int?
   (ds/req :verse) int?
   })

(def chapter-footnote-reference-spec
  (ds/spec
    {:name ::chapter-footnote-reference
     :spec chapter-footnote-reference-data}))
