# Pronouncing

[![Build Status](https://travis-ci.org/wiseman/clj-pronouncing.svg?branch=master)](https://travis-ci.org/wiseman/clj-pronouncing) [![Coverage Status](https://coveralls.io/repos/wiseman/clj-pronouncing/badge.svg?branch=master)](https://coveralls.io/r/wiseman/clj-pronouncing?branch=master)

This is a simple interface to the CMU Pronouncing Dictionary. It
provides ways to look up word pronounciations, count syllables, and
find rhyming words.

This is a port of
[Allison Parrish's Python code](https://github.com/aparrish/pronouncingpy)
and
[documentation](https://pronouncing.readthedocs.org/en/latest/tutorial.html).

## Installation

```
[com.lemonodor/pronouncing "0.0.3"]
```


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
           (map rand-nth)
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


## Meter

Pronouncing includes a number of functions to help you isolate
metrical characteristics of a text. You can use the `stresses`
function to get a string that represents the “stress pattern” of a
string of phones:

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> (pro/stresses (first (pro/phones-for-word "snappiest")))
"102"
```

A “stress pattern” is a string that contains only the stress values
from a sequence of phones. (The numbers indicate the level of stress:
1 for primary stress, 2 for secondary stress, and 0 for unstressed.)

You can use the `search_stresses` function to find words based on
their stress patterns. For example, to find words that have two
dactyls in them (“dactyl” is a metrical foot consisting of one
stressed syllable followed by two unstressed syllables):

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> (pro/search-stresses "100100")
("afroamerican" "afroamericans" "interrelationship" "overcapacity")
```

You can use regular expression syntax inside of the patterns you give
to `search-stresses`. For example, to find all words wholly consisting
of two anapests (unstressed, unstressed, stressed), with “stressed”
meaning either primary stress or secondary stress:

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> (pro/search-stresses "^00[12]00[12]$")
("neopositivist" "undercapitalize" "undercapitalized")
```

The following example rewrites a text, replacing each word with a
random word that has the same stress pattern:

```
user> (require '[com.lemonodor.pronouncing :as pro]
               '[clojure.string :as string])
user> (->> (string/split "april is the cruelest month breeding lilacs out of the dead" #" ")
           (map #(first (pro/phones-for-word %)))
           (map pro/stresses)
           (map #(pro/search-stresses (str "^" % "$")))
           (map rand-nth))
("delta" "bronx" "'em" "inzer" "denz" "sobils" "bedpan" "paiz" "bush" "can" "giang")
```


## Rhyme

Pronouncing includes a simple function, `rhymes`, which returns a list
of words that (potentially) rhyme with a given word. You can use it
like so:

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> (pro/rhymes "failings")
("mailings" "railings" "tailings")
```

The `rhymes` function returns a list of all possible rhymes for the
given word—i.e., words that rhyme with any of the given word’s
pronunciations. If you only want rhymes for one particular
pronunciation, the the `rhyming-part` function gives a smaller part of
a string of phones that can be used with `search` to find rhyming
words. The following code demonstrates how to find rhyming words for
two different pronunciations of “uses”:

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> (def pronounciations (pro/phones-for-word "uses"))
#'user/pronounciations
user> (def sss (pro/rhyming-part (first pronounciations)))
#'user/sss
user> (def zzz (pro/rhyming-part (second pronounciations)))
#'user/zzz
user> (take 5 (pro/search (str sss "$")))
("bruce's" "juices" "medusas" "produces" "tuscaloosa's")
user> (take 5 (pro/search (str zzz "$")))
("abuses" "cabooses" "disabuses" "excuses" "induces")
```

Here's how to check whether one word rhymes with another:

```
user> (require '[com.lemonodor.pronouncing :as pro])
user> ((set (pro/rhymes "cheese")) "wheeze")
"wheeze"
user> ((set (pro/rhymes "cheese")) "geese")
nil
```

The following example rewrites a text, replacing each word with a
rhyming word (when a rhyming word is available):

```
user> (require '[com.lemonodor.pronouncing :as pro]
               '[clojure.string :as string])
user> (->> (string/split "april is the cruelest month breeding lilacs out of the dead" #" ")
           (map #(vector % (pro/rhymes %)))
           (map #(let [[w rs] %] (if (seq rs) (rand-nth rs) w)))
           (string/join " "))
"april focuses shema coolest month heeding paperbacks snout aversive huh dredd"
```
