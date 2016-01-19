(ns com.lemonodor.pronouncing
  "This is a simple interface to the CMU Pronouncing Dictionary. It
  provides ways to look up word pronounciations, count syllables, and
  find rhyming words."
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))


(defn parse-cmudict-line [line]
  (let [[word phones] (string/split line #"(\(\d+\))?  ")]
    [(string/lower-case word) phones]))


(defn is-cmudict-comment-line? [line]
  (string/starts-with? line ";"))


(defn parse-cmudict [rdr]
  (->> rdr
       line-seq
       (remove is-cmudict-comment-line?)
       (map parse-cmudict-line)))


(def default-pronouncing-db
  (memoize
   (fn []
     (with-open [rdr (io/reader (io/resource "com/lemonodor/pronouncing/cmudict-0.7b"))]
       (doall (parse-cmudict rdr))))))


(def word-phones-map
  (memoize
   (fn []
     (reduce
      (fn [db [word phones]]
        (assoc db word (conj (get db word []) phones)))
      {}
      (default-pronouncing-db)))))


(defn syllable-count
  "Counts the number of syllables in a string of phones."
  [phones]
  (count (re-seq #"\S+[0-2]\b" phones)))


(defn phones-for-word
  "Returns a collection of possible phone strings for a given word.

  A word may have more than one pronounciation in the dictionary, so
  this function returns a list of all possible pronounciations."
  [word]
  ((word-phones-map) (string/lower-case word)))


(defn syllable-count-for-word
  "Returns a set of possible syllable counts for a given word.

  A word may have more than one pronounciation in the dictionary, so
  this function returns a list of all possible, distinct syllable
  counts."
  [word]
  (set (map syllable-count (phones-for-word word))))


(defn stresses
  "Returns a string of digits representing the vowel stresses for a
  given string of phones."
  [phones]
  (string/replace phones #"[^012]" ""))


(defn stresses-for-word
  "Returns a sequence of possible stress patterns for a given word."
  [word]
  (map stresses (phones-for-word word)))


(defn- take-from-last
  "Return seq of elements in collection from the last item to pass 
  predicate until the end."
  [pred coll]
  (loop [match nil
         xs coll]
    (if-not (seq xs)
      match
      (if (pred (first xs))
        (recur xs (rest xs))
        (recur match (rest xs))))))


(defn rhyming-part
  "Returns the 'rhyming part' of a phone string.

  'Rhyming part' here means everything from the vowel in the stressed
  syllable nearest the end of the word up to the end of the word."
  [phones-str]
  (->> (string/split phones-str #" ")
       (take-from-last #(re-matches #".+[12]$" %))
       (string/join " ")))


(defn search
  "Returns words whose pronounciation matches a regular expression.

  Searches the dictionary for pronounciations matching a given regular
  expression. Word boundary anchors are automatically added before and
  after the pattern."
  [regex]
  (let [r (re-pattern (str "\\b" regex "\\b"))]
    (for [[word phones] (default-pronouncing-db)
          :when (re-find r phones)]
      word)))


(defn search-stresses
  "Returns words whose stress pattern matches a regular expression.

  This is a special case of the search function that searches only the
  stress patterns of each pronounciation in the dictionary. You can
  get stress patterns for a word using the stresses-for-word
  function."
  [regex]
  (let [r (re-pattern regex)]
    (for [[word phones] (default-pronouncing-db)
          :when (re-find r (stresses phones))]
      word)))


(defn rhymes
  "Returns words that rhyme with a given word."
  [word]
  (->> word
       phones-for-word
       (mapcat
        #(-> %
             rhyming-part
             (str "$")
             search))
       (remove #{word})))
