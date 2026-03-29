(ns bible-api-client.specs.list-books-200-response
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.translation :refer :all]
            [bible-api-client.specs.translation-book :refer :all]
            )
  (:import (java.io File)))


(def list-books-200-response-data
  {
   (ds/req :translation) translation-spec
   (ds/req :books) (s/coll-of translation-book-spec)
   })

(def list-books-200-response-spec
  (ds/spec
    {:name ::list-books-200-response
     :spec list-books-200-response-data}))
