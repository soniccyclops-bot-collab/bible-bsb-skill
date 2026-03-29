(defproject bible-api-client "1.11.0"
  :description "Public, read-only JSON API serving Bible translations, books, and chapter content from helloao.org. No authentication required."
  :license {:name "Various (per translation)"
            :url "https://helloao.org"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [metosin/spec-tools "0.7.0"]
                 [clj-http "3.8.0"]
                 [orchestra "2017.11.12-1"]
                 [cheshire "5.8.0"]])