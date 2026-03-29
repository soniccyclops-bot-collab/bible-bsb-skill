(ns bible-api-client.specs.translation-complete
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.translation :refer :all]
            [bible-api-client.specs.translation-complete-book :refer :all]
            )
  (:import (java.io File)))


(def translation-complete-data
  {
   (ds/req :translation) translation-spec
   (ds/req :books) (s/coll-of translation-complete-book-spec)
   })

(def translation-complete-spec
  (ds/spec
    {:name ::translation-complete
     :spec translation-complete-data}))
