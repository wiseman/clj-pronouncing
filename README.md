# Pronouncing

[![Build Status](https://travis-ci.org/wiseman/clj-pronouncing.svg?branch=master)](https://travis-ci.org/wiseman/clj-pronouncing) [![Coverage Status](https://coveralls.io/repos/wiseman/clj-pronouncing/badge.svg?branch=master)](https://coveralls.io/r/wiseman/clj-pronouncing?branch=master)

This is a simple interface to the CMU Pronouncing Dictionary. It
provides ways to look up word pronounciations, count syllables, and
find rhyming words.

## Word pronounciations

Let’s start by using Pronouncing to get the pronunciation for a given
word. Here’s the code:

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> (pro/phones-for-word "permit")
("P ER0 M IH1 T" "P ER1 M IH2 T")
```

The `phones-for-word` function returns a list of all pronunciations
for the given word found in the CMU pronouncing dictionary.
Pronunciations are given using a special phonetic alphabet known as
ARPAbet.
[Here’s a list of ARPAbet symbols and what English sounds they stand for](http://www.speech.cs.cmu.edu/cgi-bin/cmudict#phones).
Each token in a pronunciation string is called a “phone.” The numbers
after the vowels indicate the vowel’s stress. The number 1 indicates
primary stress; 2 indicates secondary stress; and 0 indicates
unstressed.
([Wikipedia has a good overview of how stress works in English](https://en.wikipedia.org/wiki/Stress_and_vowel_reduction_in_English),
if you’re interested.)

Sometimes, the pronouncing dictionary has more than one pronunciation
for the same word. “Permit” is a good example: it can be pronounced
either with the stress on the first syllable (“do you have a permit to
program here?”) or on the second syllable (“will you permit me to
program here?”). For this reason, the `phones_for_word` function
returns a list of possible pronunciations. (You’ll need to come up
with your own criteria for deciding which pronunciation is best for
your purposes.)

Here’s how to calculate the most common sounds in a given text:

```
user> (require '[com.lemonodor.pronouncing :as pro]
               '[clojure.string :as string])
user> (->> (string/split "april is the cruelest month breeding lilacs out of the dead" #" ")
           (map #(first (pro/phones-for-word %)))
           (mapcat #(string/split % #" "))
           frequencies
           (sort-by val)
           reverse
           (take 5))
(["AH0" 4] ["L" 4] ["D" 3] ["R" 3] ["DH" 2])
```

## Pronounciation search

Pronouncing has a helpful function `search` which allows you to search
the pronouncing dictionary for words whose pronunciation matches a
particular regular expression. For example, to find words that have
within them the same sounds as the word “sighs”:

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> (->> "sighs"
           pro/phones-for-word
           first
           pro/search
           (take 5))
("incise" "incised" "incisor" "incisors" "malloseismic")
```

For convenience, word-boundary anchors (\b) are added automatically to
the beginning and end of the pattern you pass to `search`. You’re free
to include any other regular expression syntax in the pattern. Here’s
another example, which finds words that end in “-iddle”:

```
user> (pro/search "IH1 D AH0 L$")
("biddle" "criddle" "fiddle" "friddle" "kiddle" "liddell" "liddle" "middle"
 "piddle" "riddell" "riddle" "rydell" "schmidl" "siddall" "siddell" "siddle"
 "spidel" "spidell" "twiddle" "widdle" "widell")
```

Another example, which re-writes a text by taking each word and
replacing it with a random word that begins with the same first two
phones:

```
user> (require '[com.lemonodor.pronouncing :as pro]
               '[clojure.string :as string])
user> (->> (string/split "april is the cruelest month breeding lilacs out of the dead" #" ")
           (map #(first (pro/phones-for-word %)))
           (map #(take 2 (string/split % #" ")))
           (map #(string/join " " %))
           (map #(pro/search (str "^" %)))
           (map #(rand-nth %))
           (string/join " "))
"apec israel's the critchfield munsch brainer lionize outtakes ovens themselves debs"
```

## Counting syllables

To get the number of syllables in a word, first get one of its
pronunciations with `phones-for-word` and pass the resulting string of
phones to the `syllable-count` function, like so:

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> (-> "programming"
          pro/phones-for-word
          first
          pro/syllable-count)
3
```

The following example calculates the total number of syllables in a
text (assuming that all of the words are found in the pronouncing
dictionary, and using the first pronounciation if there are multiple
pronounciations):

```
user> (require '[com.lemonodor.pronouncing :as pro]
               '[clojure.string :as string])
user> (->> (string/split "april is the cruelest month breeding lilacs out of the dead" #" ")
           (map #(first (pro/phones-for-word %)))
           (map pro/syllable-count)
           (reduce +))
15
```
