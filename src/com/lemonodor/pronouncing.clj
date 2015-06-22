(ns com.lemonodor.pronouncing
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
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


(defn make-syllable-db-from-cmudict [path]
  (with-open [rdr (io/reader path)]
    (apply
     merge-with
     set/union
     (map (fn [[word & phonemes]]
            {word #{(count-cmudict-syllables phonemes)}})
          (map parse-cmudict-line
               (filter
                (fn [^String l] (not (= (.charAt l 0) \;)))
                (line-seq rdr)))))))


(def default-pronouncing-db
  (memoize
   (fn []
     (with-open [rdr (io/reader (io/resource "com/lemonodor/pronouncing/cmudict-0.7b"))]
       (reduce
        (fn [db [word phones]]
          (assoc db word
                 (concat (get db word []) (vector phones))))
        {}
        (parse-cmudict rdr))))))


(defn phones-for-word [word]
  ((default-pronouncing-db) (string/lower-case word)))


(defn count-syllables
  ([word]
   (count-syllables (default-pronouncing-db) word))
  ([sdb word]
   (sdb (string/lower-case word))))


(defn -main [& args]
  (let [sdb (make-syllable-db-from-cmudict (first args))]
    (binding [*out* *err*]
      (println "Creating syllable database with" (count sdb) "words."))
    (prn sdb)))
