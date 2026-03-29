(ns bible-api-client.specs.chapter-footnote-caller
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            )
  (:import (java.io File)))


(def chapter-footnote-caller-data
  {
   })

(def chapter-footnote-caller-spec
  (ds/spec
    {:name ::chapter-footnote-caller
     :spec chapter-footnote-caller-data}))
