(ns bible-api-client.specs.list-commentary-books-200-response
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.commentary :refer :all]
            [bible-api-client.specs.commentary-book :refer :all]
            )
  (:import (java.io File)))


(def list-commentary-books-200-response-data
  {
   (ds/req :commentary) commentary-spec
   (ds/req :books) (s/coll-of commentary-book-spec)
   })

(def list-commentary-books-200-response-spec
  (ds/spec
    {:name ::list-commentary-books-200-response
     :spec list-commentary-books-200-response-data}))
