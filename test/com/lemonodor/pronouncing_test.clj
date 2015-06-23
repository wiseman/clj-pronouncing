(ns com.lemonodor.pronouncing-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [com.lemonodor.pronouncing :as pronouncing]))

(deftest cmudict-test
  (testing "parse-cmudict-line"
    (is (= ["\"unquote" "AH1 N K W OW1 T"]
           (pronouncing/parse-cmudict-line "\"UNQUOTE  AH1 N K W OW1 T")))
    (is (= ["aaa" "T R IH2 P AH0 L EY1"]
           (pronouncing/parse-cmudict-line "AAA  T R IH2 P AH0 L EY1")))
    (is (= ["aaronson's" "AA1 R AH0 N S AH0 N Z"]
           (pronouncing/parse-cmudict-line
            "AARONSON'S(1)  AA1 R AH0 N S AH0 N Z")))
    (is (= ["abkhazian" "AE0 B K AE1 Z Y AH0 N"]
           (pronouncing/parse-cmudict-line
            "ABKHAZIAN(3)  AE0 B K AE1 Z Y AH0 N"))))
  (testing "parse-cmudict"
    (let [cmudict (str ";; Test dict\n"
                       "LLAMAS  L AA1 M AH0 Z\n"
                       "LOUIS'(1)  L UW1 IY0 Z\n"
                       "LOUIS'(2)  L UW1 IH0 S IH0 Z\n")
          db (pronouncing/parse-cmudict (io/reader (.getBytes cmudict)))]
      (is (= [["llamas" "L AA1 M AH0 Z"]
              ["louis'" "L UW1 IY0 Z"]
              ["louis'" "L UW1 IH0 S IH0 Z"]]
             db))))
  (testing "word-phones-map"
    (let [db (pronouncing/word-phones-map)]
      (is (= ["JH AA1 N"]
             (db "john")))
      (is (= ["L UW1 IH0 S"
              "L UW1 IY0 Z"
              "L UW1 IH0 S IH0 Z"]
             (db "louis'"))))))

(deftest phones-for-word
  (testing "phones for word"
    (is (= ["P ER0 M IH1 T"
            "P ER1 M IH2 T"]
           (pronouncing/phones-for-word "permit")))))


(deftest search
  (testing "search"
    (is (= ["all-purpose" "interpolate" "interpolated" "multipurpose"
            "perpetrate" "perpetrated" "perpetrates" "perpetrating"
            "perpetrator" "perpetrator's" "perpetrators" "proserpina"
            "purpa" "purple" "purples" "purpose" "purposeful" "purposefully"
            "purposeless" "purposely" "purposes" "purposes" "serpas" "serpent"
            "serpent's" "serpentine" "serpents" "terpening" "tirpak" "turpen"
            "turpentine"]
           (pronouncing/search "ER1 P AH0")))))
