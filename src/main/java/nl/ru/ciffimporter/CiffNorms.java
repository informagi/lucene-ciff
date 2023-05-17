package nl.ru.ciffimporter;

import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.SmallFloat;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffNorms extends NumericDocValues {

    private final DocRecord[] docRecords;
    private int docID = -1;

    public CiffNorms(DocRecord[] docRecords) {
        this.docRecords = docRecords;
    }

    @Override
    public long longValue() {
        return SmallFloat.intToByte4(docRecords[docID].getDoclength());
    }

    @Override
    public boolean advanceExact(int target) {
        this.docID = target;
        return docID >= 0 && docID < docRecords.length;
    }

    @Override
    public int docID() {
        return docID;
    }

    @Override
    public int nextDoc() {
        docID++;

        if (docID >= docRecords.length) {
            docID = DocIdSetIterator.NO_MORE_DOCS;
        }

        return docID;
    }

    @Override
    public int advance(int target) {
        if (advanceExact(target)) {
            return target;
        } else {
            return DocIdSetIterator.NO_MORE_DOCS;
        }
    }

    @Override
    public long cost() {
        return docRecords.length;
    }
}
