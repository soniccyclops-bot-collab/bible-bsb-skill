(ns bible-api-client.specs.list-dataset-books-200-response
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.dataset :refer :all]
            [bible-api-client.specs.dataset-book :refer :all]
            )
  (:import (java.io File)))


(def list-dataset-books-200-response-data
  {
   (ds/req :dataset) dataset-spec
   (ds/req :books) (s/coll-of dataset-book-spec)
   })

(def list-dataset-books-200-response-spec
  (ds/spec
    {:name ::list-dataset-books-200-response
     :spec list-dataset-books-200-response-data}))
