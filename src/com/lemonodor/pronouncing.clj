(ns com.lemonodor.pronouncing
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:gen-class))


(defn parse-cmudict-line [line]
  (let [[word phones] (string/split line #"(\(\d+\))?  ")]
    [(string/lower-case word) phones]))


(defn is-cmudict-comment-line? [line]
  (= (get line 0) \;))


(defn parse-cmudict [rdr]
  (->> rdr
       line-seq
       (filter #(not (is-cmudict-comment-line? %)))
       (map parse-cmudict-line)))


(def digit-chars #{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9})


(defn count-cmudict-syllables [phonemes]
  (count
   (filter
    (fn [^String p]
      (digit-chars (.charAt p (dec (.length p)))))
    phonemes)))


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
        (assoc db word (concat (get db word []) (vector phones))))
      {}
      (default-pronouncing-db)))))


(defn phones-for-word [word]
  ((word-phones-map) (string/lower-case word)))


(defn search [regex]
  (let [r (re-pattern (str "\\b" regex "\\b"))]
    (for [[word phones] (default-pronouncing-db)
          :when (re-find r phones)]
      word)))


(defn stresses [phones]
  (string/replace phones #"[^012]" ""))


(defn search-stresses [regex]
  (let [r (re-pattern regex)]
    (for [[word phones] (default-pronouncing-db)
          :when (re-find r (stresses phones))]
      word)))


(defn syllable-count [phones]
  (count (filter #(#{\0 \1 \2} %) phones)))


(defn count-syllables
  ([word]
   (count-syllables (default-pronouncing-db) word))
  ([sdb word]
   (sdb (string/lower-case word))))
