

## Install

1) Download core: http://kaiko.getalp.org/about-dbnary/download/ (en_dbnary_ontolex.ttl.bz2)
- en
- de
- es
- pt
- ru
- nl
 -fr
You need only core - not disambiguation translation! 
 
2) Download tdb https://jena.apache.org/download/index.cgi and add to path.
3) `tdbloader2 --loc ./ <path to en_dbnary_ontolex.ttl.bz2> <path to pt_dbnary_ontolex.ttl.bz2> ...`
Note that if you do not load all files with `tdbloader2` at once, you can only add with `tdbloader`.

2020 done
- en
- de
- es
- pt
- ru
- nl
 -fr