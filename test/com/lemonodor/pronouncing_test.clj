(ns com.lemonodor.pronouncing-test
  (:require [clojure.test :refer :all]
            [com.lemonodor.pronouncing :as pronouncing]))

(deftest cmudict-test
  (testing "parse-cmudict-line"
    (is (= ["\"unquote" "ah1" "n" "k" "w" "ow1" "t"]
           (pronouncing/parse-cmudict-line "\"UNQUOTE  AH1 N K W OW1 T")))
    (is (= ["aaa" "t" "r" "ih2" "p" "ah0" "l" "ey1"]
           (pronouncing/parse-cmudict-line "AAA  T R IH2 P AH0 L EY1")))
    (is (= ["aaronson's" "aa1" "r" "ah0" "n" "s" "ah0" "n" "z"]
           (pronouncing/parse-cmudict-line
            "AARONSON'S(1)  AA1 R AH0 N S AH0 N Z")))
    (is (= ["abkhazian" "ae0" "b" "k" "ae1" "z" "y" "ah0" "n"]
           (pronouncing/parse-cmudict-line
            "ABKHAZIAN(3)  AE0 B K AE1 Z Y AH0 N"))))
  (testing "count-cmudict-pronouncing"
    (is (= 3
           (pronouncing/count-cmudict-pronouncing
            ["m" "ah0" "k" "aa1" "r" "th" "iy0"])))
    (is (= 1 (pronouncing/count-cmudict-pronouncing ["l" "ih1" "s" "p"]))))
  (testing "make-syllable-db-from-cmudict"
    (let [cmudict (str ";; Test dict\n"
                       "LLAMAS  L AA1 M AH0 Z\n"
                       "LOUIS'(1)  L UW1 IY0 Z\n"
                       "LOUIS'(2)  L UW1 IH0 S IH0 Z\n")
          sdb (pronouncing/make-syllable-db-from-cmudict (.getBytes cmudict))]
      (is (= {"louis'" #{3 2}, "llamas" #{2}} sdb))
      (is (= #{2 3} (pronouncing/count-pronouncing sdb "louis'")))
      (is (= #{2 3} (pronouncing/count-pronouncing sdb "LouIS'")))))
  (testing "count-pronouncing"
    (is (= #{4} (pronouncing/count-pronouncing "ridiculous")))
    (is (= nil (pronouncing/count-pronouncing "........")))))
