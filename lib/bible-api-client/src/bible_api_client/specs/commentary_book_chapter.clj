(ns bible-api-client.specs.commentary-book-chapter
  (:require [clojure.spec.alpha :as s]
            [spec-tools.data-spec :as ds]
            [bible-api-client.specs.commentary-chapter :refer :all]
            [bible-api-client.specs.commentary :refer :all]
            [bible-api-client.specs.commentary-book :refer :all]
            )
  (:import (java.io File)))


(def commentary-book-chapter-data
  {
   (ds/req :chapter) commentary-chapter-spec
   (ds/req :commentary) commentary-spec
   (ds/req :book) commentary-book-spec
   (ds/req :thisChapterLink) string?
   (ds/req :nextChapterApiLink) string?
   (ds/req :previousChapterApiLink) string?
   (ds/req :numberOfVerses) int?
   })

(def commentary-book-chapter-spec
  (ds/spec
    {:name ::commentary-book-chapter
     :spec commentary-book-chapter-data}))
