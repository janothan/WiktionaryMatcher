# Wiktionary Matcher
This matcher participated in the <a href="http://oaei.ontologymatching.org/">OAEI</a> campaigns <a href="http://oaei.ontologymatching.org/2019/">2019</a> and 
<a href="http://oaei.ontologymatching.org/2020/">2020</a>.
The matcher is implemented with the <a href="https://github.com/dwslab/melt/">Matching and EvaLuation Toolkit (MELT)</a> and
can be packaged for SEALS.

**How to Cite?**<br/>
```
 Portisch, Jan; Hladik, Michael; Paulheim, Heiko. Wiktionary Matcher. CEUR Workshop Proceedings OM 2019 - Proceedings of the 14th International Workshop on Ontology Matching co-located with the 18th International Semantic Web Conference (ISWC 2019). Auckland, New Zealand. October 26, 2019. Pages 181 - 188.
```
An open-access version of the paper can be found <a href="http://ceur-ws.org/Vol-2536/oaei19_paper15.pdf">here</a>.

## Installation / Setup

**(1) Download Wiktionary Files**

Download core: http://kaiko.getalp.org/about-dbnary/download/ (en_dbnary_ontolex.ttl.bz2)
- en
- de
- es
- pt
- ru
- nl
- fr

You need only core - not disambiguation translation! 
 
**(2) Download tdb**
 
Download <a href="https://jena.apache.org/download/index.cgi">https://jena.apache.org/download/index.cgi </a> and add it 
to path.

**(3) Load Data with tdbloader**
``
```
tdbloader2 --loc ./ <path to en_dbnary_ontolex.ttl.bz2> <path to pt_dbnary_ontolex.ttl.bz2> ...
```
Note that if you do not load all files with `tdbloader2` at once, you can only add with `tdbloader`.

**(4) Create `oaei-resources`**
- In the project create `/oaei-resources/wiktionary-tdb/` and place the database files obtained in (3)
there.
- In the project create `/oaei-resources/stopwords/` and place a file named `english_stopwords.txt` in there.
It should contain one stopword per line (e.g. `a`, `the`).

## Future Improvements
This matcher uses <a>DBnary</a> as general knowledge background source. Due to restrictions of the extraction framework
the following relations are not extracted albeit present on Wiktionary and helpful for matching:
- derived terms
- alternative forms

They will be added to this matcher when they are available (two enhancement requests have been submitted on the 
<a href="https://bitbucket.org/serasset/dbnary/issues?status=new&status=open">dbnary bitbucket</a>).