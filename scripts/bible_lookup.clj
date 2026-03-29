#!/usr/bin/env bb
;; bible_lookup.clj — Bible verse lookup via helloao.org API
;; Usage: bb bible_lookup.clj "John 3:16" [--translation KJV] [--study] [--compare] [--cross-refs] [--expand] [--commentary [name]]

(require '[babashka.http-client :as http]
         '[cheshire.core :as json]
         '[clojure.string :as str])

;; ---------------------------------------------------------------------------
;; Book name → API ID mapping
;; ---------------------------------------------------------------------------

(def books
  {"genesis" "GEN" "exodus" "EXO" "leviticus" "LEV" "numbers" "NUM"
   "deuteronomy" "DEU" "joshua" "JOS" "judges" "JDG" "ruth" "RUT"
   "1 samuel" "1SA" "2 samuel" "2SA" "1 kings" "1KI" "2 kings" "2KI"
   "1 chronicles" "1CH" "2 chronicles" "2CH" "ezra" "EZR" "nehemiah" "NEH"
   "esther" "EST" "job" "JOB" "psalm" "PSA" "psalms" "PSA"
   "proverbs" "PRO" "ecclesiastes" "ECC" "song of solomon" "SNG"
   "song of songs" "SNG" "isaiah" "ISA" "jeremiah" "JER"
   "lamentations" "LAM" "ezekiel" "EZK" "daniel" "DAN" "hosea" "HOS"
   "joel" "JOL" "amos" "AMO" "obadiah" "OBA" "jonah" "JON"
   "micah" "MIC" "nahum" "NAM" "habakkuk" "HAB" "zephaniah" "ZEP"
   "haggai" "HAG" "zechariah" "ZEC" "malachi" "MAL"
   "matthew" "MAT" "mark" "MRK" "luke" "LUK" "john" "JHN"
   "acts" "ACT" "romans" "ROM" "1 corinthians" "1CO" "2 corinthians" "2CO"
   "galatians" "GAL" "ephesians" "EPH" "philippians" "PHP" "colossians" "COL"
   "1 thessalonians" "1TH" "2 thessalonians" "2TH"
   "1 timothy" "1TI" "2 timothy" "2TI" "titus" "TIT" "philemon" "PHM"
   "hebrews" "HEB" "james" "JAS" "1 peter" "1PE" "2 peter" "2PE"
   "1 john" "1JN" "2 john" "2JN" "3 john" "3JN" "jude" "JUD"
   "revelation" "REV"})

;; Reverse lookup: ID → display name
(def book-id->name
  (into {} (for [[name id] books
                 :when (not (#{"psalms" "song of songs"} name))]
             [id (str/join " " (map str/capitalize (str/split name #" ")))])))

;; Abbreviation matching: build prefix map
(defn resolve-book [s]
  (let [lower (str/lower-case (str/trim s))]
    (or (books lower)
        ;; Try prefix matching
        (some (fn [[name id]]
                (when (str/starts-with? name lower) id))
              (sort-by (comp count first) books))
        ;; Try with/without number prefix: "1cor" → "1 corinthians"
        (when-let [[_ num rest] (re-matches #"(\d)\s*(.*)" lower)]
          (some (fn [[name id]]
                  (when (and (str/starts-with? name (str num " "))
                             (str/starts-with? (subs name 2) rest))
                    id))
                books)))))

;; ---------------------------------------------------------------------------
;; Reference parsing
;; ---------------------------------------------------------------------------

(defn parse-reference [ref]
  (let [ref (str/trim ref)
        ;; Match: "Book Chapter:VerseStart-VerseEnd" or "Book Chapter:Verse" or "Book Chapter"
        ;; Handle numbered books: "1 Corinthians 13:4-8"
        [_ book-part chapter-part]
        (re-matches #"(?i)((?:\d\s+)?[a-z][a-z\s]+?)\s+(\d+(?::\d+(?:-\d+)?)?)" ref)]
    (when (and book-part chapter-part)
      (let [book-id (resolve-book book-part)
            parts (str/split chapter-part #"[:\-]")
            chapter (Integer/parseInt (first parts))
            verse-start (when (>= (count parts) 2) (Integer/parseInt (second parts)))
            verse-end (when (>= (count parts) 3) (Integer/parseInt (nth parts 2)))]
        (when book-id
          {:book-id book-id
           :chapter chapter
           :verse-start (or verse-start nil)
           :verse-end (or verse-end verse-start)})))))

;; ---------------------------------------------------------------------------
;; Translation aliases
;; ---------------------------------------------------------------------------

(def translation-aliases
  {"kjv" "eng_kjv" "web" "ENGWEBP" "engwebp" "ENGWEBP"
   "bsb" "BSB" "esv" "ESV" "niv" "NIV"})

(defn resolve-translation [t]
  (get translation-aliases (str/lower-case t) t))

;; ---------------------------------------------------------------------------
;; API calls
;; ---------------------------------------------------------------------------

(def base-url "https://bible.helloao.org/api")

(defn fetch-json [url]
  (let [resp (http/get url {:throw false})]
    (when (= 200 (:status resp))
      (json/parse-string (:body resp) true))))

(defn fetch-chapter [translation book-id chapter]
  (fetch-json (str base-url "/" (resolve-translation translation) "/" book-id "/" chapter ".json")))

(defn fetch-commentary-chapter [commentary-id book-id chapter]
  (fetch-json (str base-url "/c/" commentary-id "/" book-id "/" chapter ".json")))

(defn fetch-cross-refs [book-id chapter]
  (fetch-json (str base-url "/d/open-cross-ref/" book-id "/" chapter ".json")))

;; ---------------------------------------------------------------------------
;; Content extraction
;; ---------------------------------------------------------------------------

(defn extract-verse-text
  "Extract text from a verse content array.
   Items can be: strings, maps with :text (formatted text), maps with :noteId (footnotes),
   maps with :lineBreak (line breaks)."
  [content & {:keys [include-notes] :or {include-notes false}}]
  (str/join
   (map (fn [item]
          (cond
            (string? item) item
            (and (map? item) (:text item)) (if (= 2 (:poem item))
                                             (str "\n " (:text item))
                                             (:text item))
            (and (map? item) (:lineBreak item)) "\n"
            (and include-notes (map? item) (:noteId item)) (str " [" (:noteId item) "] ")
            (and (map? item) (:noteId item)) " "  ;; preserve space where footnote marker was
            :else ""))
        content)))

(defn format-verses
  "Format verses from chapter data."
  [data verse-start verse-end & {:keys [study] :or {study false}}]
  (let [content (get-in data [:chapter :content])
        footnotes (get-in data [:chapter :footnotes])
        note-ids (atom #{})
        lines (atom [])]
    (doseq [[idx item] (map-indexed vector content)]
      (case (:type item)
        "heading" (when (nil? verse-start)
                   (let [heading-text (str/join (map #(if (string? %) % (or (:text %) "")) (:content item)))]
                     (swap! lines conj (str "\n  [" heading-text "]"))))
        "verse" (let [num (:number item)]
                  (when (and (or (nil? verse-start) (>= num verse-start))
                             (or (nil? verse-end) (<= num verse-end)))
                    (let [text (extract-verse-text (:content item) :include-notes study)]
                      ;; Track note IDs for footnote display
                      (when study
                        (doseq [c (:content item)]
                          (when (and (map? c) (:noteId c))
                            (swap! note-ids conj (:noteId c)))))
                      (swap! lines conj (str "  " num "  " text)))))
        nil))
    ;; Append footnotes if study mode
    (when (and study (seq @note-ids))
      (swap! lines conj "\n  Footnotes:")
      (doseq [fn footnotes
              :when (contains? @note-ids (:noteId fn))]
        (swap! lines conj (str "    [" (:noteId fn) "] ("
                              (:chapter (:reference fn)) ":" (:verse (:reference fn))
                              ") " (:text fn)))))
    (str/join "\n" @lines)))

(defn format-citation [book-id chapter verse-start verse-end translation]
  (let [name (book-id->name book-id book-id)]
    (if verse-start
      (if (and verse-end (not= verse-start verse-end))
        (str name " " chapter ":" verse-start "-" verse-end " (" translation ")")
        (str name " " chapter ":" verse-start " (" translation ")"))
      (str name " " chapter " (" translation ")"))))

;; ---------------------------------------------------------------------------
;; Commentary (dynamic lookup)
;; ---------------------------------------------------------------------------

(defn resolve-commentary [name]
  (let [lower (str/lower-case (str/trim name))
        commentaries (-> (fetch-json (str base-url "/available_commentaries.json"))
                         :commentaries)]
    (or
     ;; Exact match on ID
     (some #(when (= lower (str/lower-case (:id %))) (:id %)) commentaries)
     ;; Partial match on id/name/englishName
     (some #(when (or (str/includes? (str/lower-case (or (:id %) "")) lower)
                      (str/includes? (str/lower-case (or (:name %) "")) lower)
                      (str/includes? (str/lower-case (or (:englishName %) "")) lower))
               (:id %))
           commentaries)
     ;; Fallback
     name)))

(defn display-commentary [commentary-id book-id chapter verse-start verse-end]
  (when-let [data (fetch-commentary-chapter commentary-id book-id chapter)]
    (let [content (get-in data [:chapter :content])]
      (println (str "\n  Commentary (" commentary-id "):"))
      (doseq [item content
              :when (= "verse" (:type item))
              :when (and (or (nil? verse-start) (>= (:number item) verse-start))
                         (or (nil? verse-end) (<= (:number item) verse-end)))]
        (let [text (extract-verse-text (:content item))]
          (when (seq (str/trim text))
            (println (str "\n  " (:number item) "  " text))))))))

;; ---------------------------------------------------------------------------
;; Cross-references
;; ---------------------------------------------------------------------------

(defn display-cross-refs [book-id chapter verse-start verse-end & {:keys [expand translation]
                                                                    :or {expand false translation "BSB"}}]
  (when-let [data (fetch-cross-refs book-id chapter)]
    (let [content (get-in data [:chapter :content])
          relevant (filter (fn [item]
                            (let [v (:verse item)]
                              (and (or (nil? verse-start) (>= v verse-start))
                                   (or (nil? verse-end) (<= v verse-end)))))
                          content)
          all-refs (mapcat :references relevant)]
      (println "\n  Cross-references:")
      (let [refs-to-show (take 8 all-refs)
            ;; Group by book+chapter to minimize fetches
            chapter-cache (atom {})]
        (doseq [{:keys [book chapter verse]} refs-to-show]
          (let [display-name (book-id->name book book)
                ref-str (str "    - " display-name " " chapter ":" verse)]
            (if expand
              (let [cache-key [book chapter]
                    ch-data (or (get @chapter-cache cache-key)
                                (let [d (fetch-chapter translation book chapter)]
                                  (swap! chapter-cache assoc cache-key d)
                                  d))]
                (if ch-data
                  (let [verse-item (->> (get-in ch-data [:chapter :content])
                                        (filter #(and (= "verse" (:type %))
                                                      (= verse (:number %))))
                                        first)]
                    (if verse-item
                      (println (str ref-str "  " (extract-verse-text (:content verse-item))))
                      (println ref-str)))
                  (println ref-str)))
              (println ref-str))))))))

;; ---------------------------------------------------------------------------
;; Compare mode
;; ---------------------------------------------------------------------------

(defn compare-mode [book-id chapter verse-start verse-end]
  (doseq [tr ["BSB" "KJV" "ENGWEBP"]]
    (when-let [data (fetch-chapter tr book-id chapter)]
      (let [citation (format-citation book-id chapter verse-start verse-end tr)]
        (println (str "\n--- " citation " ---"))
        (println (format-verses data verse-start verse-end))))))

;; ---------------------------------------------------------------------------
;; CLI argument parsing
;; ---------------------------------------------------------------------------

(defn parse-args [args]
  (loop [args args
         opts {:translation "BSB"}]
    (if (empty? args)
      opts
      (let [[arg & rest] args]
        (cond
          (= "--translation" arg) (recur (next rest) (assoc opts :translation (first rest)))
          (= "--study" arg) (recur rest (assoc opts :study true))
          (= "--compare" arg) (recur rest (assoc opts :compare true))
          (= "--cross-refs" arg) (recur rest (assoc opts :cross-refs true))
          (= "--expand" arg) (recur rest (assoc opts :expand true))
          (= "--commentary" arg) (if (and (seq rest) (not (str/starts-with? (first rest) "--")))
                                   (recur (next rest) (assoc opts :commentary (first rest)))
                                   (recur rest (assoc opts :commentary "john-gill")))
          (not (str/starts-with? arg "--")) (recur rest (assoc opts :reference arg))
          :else (recur rest opts))))))

;; ---------------------------------------------------------------------------
;; Main
;; ---------------------------------------------------------------------------

(defn -main [& args]
  (when (empty? args)
    (println "Usage: bb bible_lookup.clj \"John 3:16\" [--translation KJV] [--study] [--compare] [--cross-refs] [--expand] [--commentary [name]]")
    (System/exit 1))

  (let [opts (parse-args args)
        ref (:reference opts)
        parsed (parse-reference ref)]

    (when-not parsed
      (println (str "Error: could not parse reference \"" ref "\""))
      (System/exit 1))

    (let [{:keys [book-id chapter verse-start verse-end]} parsed]

      (cond
        ;; Compare mode
        (:compare opts)
        (compare-mode book-id chapter verse-start verse-end)

        ;; Commentary mode
        (:commentary opts)
        (let [commentary-id (resolve-commentary (:commentary opts))
              data (fetch-chapter (:translation opts) book-id chapter)]
          (when data
            (let [citation (format-citation book-id chapter verse-start verse-end (:translation opts))]
              (println (str "\n" citation "\n"))
              (println (format-verses data verse-start verse-end :study (:study opts)))
              (display-commentary commentary-id book-id chapter verse-start verse-end))))

        ;; Default mode (with optional study, cross-refs)
        :else
        (let [data (fetch-chapter (:translation opts) book-id chapter)]
          (when-not data
            (println "Error: could not fetch chapter from API")
            (System/exit 1))
          (let [citation (format-citation book-id chapter verse-start verse-end (:translation opts))]
            (println (str "\n" citation "\n"))
            (println (format-verses data verse-start verse-end :study (:study opts)))
            (when (:cross-refs opts)
              (display-cross-refs book-id chapter verse-start verse-end
                                 :expand (:expand opts)
                                 :translation (:translation opts)))))))))

(apply -main *command-line-args*)
