Lucene CIFF import
==================

Application for importing a CIFF index in Lucene (and Anserini)

Preparations:
-------------

Install [Anserini](https://github.com/castorini/anserini)

Install [ciff tools](https://github.com/osirrc/ciff.git)

Set paths as follows:

    $ export PATH=$PATH:$HOME/Workspace/anserini/target/appassembler/bin
    $ export PATH=$PATH:$HOME/Workspace/ciff/target/appassembler/bin

Usage:
------

### 1. Make an index in CIFF, e.g. by using Anserini

    $ IndexCollection -collection TrecCollection -index anseriniIndex \
        -input src/test/resources/test_docs/ -optimize -threads 1 \
        -stemmer none -keepStopwords

    $ IndexReaderUtils -index anseriniIndex/ -stats

    $ SearchCollection -index anseriniIndex -topicreader Trec \
        -stemmer none -keepstopwords -qljm -output anseriniIndex.run \
        -topics src/test/resources/test_topics.txt

    $ ExportAnseriniLuceneIndex -output anseriniIndex.ciff \
        -index anseriniIndex -description "Test description"

## 2. Hook into Lucene to turn the CIFF file into a Lucene index

To generate a Lucene index from CIFF files, we make use of Lucene's
[codecs](https://lucene.apache.org/core/9_5_0/core/org/apache/lucene/codecs/package-summary.html)
module, which offers an API for reading and writing parts of the index
in different codecs. We create wrappers around the 'reading' part of the
codecs module, that read from CIFF and offer the data in the correct format
(such as `TermsEnum`, `Fields`, `DocValues`). We then use the 'writing' part
of the codecs module to write the data into an index that Lucene can read
later on.

Because we convert the CIFF data to the internal Lucene formats, the import
is codec agnostic. In other words, we can write the index in different
codecs, such as the [SimpleText
codec](https://blog.mikemccandless.com/2010/10/lucenes-simpletext-codec.html)
for easy debugging, or the Lucene90 codec for efficient retrieval.

If we import a CIFF index into Lucene, and then re-export it, we see that
the result is identical to the original CIFF file.

*Note: the `-description` argument to `ExportAnseriniLuceneIndex` must be identical to
the description in the original CIFF file.*

    $ mvn compile exec:java \
        -Dexec.mainClass="nl.ru.ciffimporter.CiffImporter" \
        -Dexec.args="anseriniIndex.ciff luceneIndex"

    $ ExportAnseriniLuceneIndex -output luceneIndex.ciff \
        -index luceneIndex -description "Test description"

    $ diff -s anseriniIndex.ciff luceneIndex.ciff
    Files anseriniIndex.ciff and luceneIndex.ciff are identical
