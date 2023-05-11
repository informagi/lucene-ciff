package nl.ru.ciffimporter;


import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffDocValues extends BinaryDocValues {

    private final DocRecord[] docRecords;
    private int docID = -1;

    public CiffDocValues(DocRecord[] docRecords) {
        this.docRecords = docRecords;
    }

    @Override
    public BytesRef binaryValue() {
        return new BytesRef(docRecords[docID].getCollectionDocid());
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
