package nl.ru.ciffimporter;

import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.SmallFloat;

import java.io.IOException;
import java.util.Iterator;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffNorms extends NumericDocValues {

    private int numDocs;
    private int[] lengths;

    private int docID = -1;

    public CiffNorms(String input) throws IOException {
        try (CiffReader reader = new CiffReader(input, true)) {
            this.numDocs = reader.getHeader().getNumDocs();
            this.lengths = readLengths(reader);
        }
    }

    public void reset() {
        this.docID = -1;
    }

    private int[] readLengths(CiffReader reader) {
        int[] lengths = new int[numDocs];

        for (Iterator<DocRecord> docRecords = reader.getDocRecords(); docRecords.hasNext(); ) {
            DocRecord doc = docRecords.next();
            lengths[doc.getDocid()] = doc.getDoclength();
        }

        return lengths;
    }

    private boolean isValidDocID(int docID) {
        return 0 <= docID && docID < numDocs;
    }

    @Override
    public long longValue() {
        int length = isValidDocID(docID) ? lengths[docID] : -1;
        return SmallFloat.intToByte4(length);
    }

    @Override
    public boolean advanceExact(int target) {
        if (isValidDocID(target) && target >= docID) {
            docID = target;
        } else {
            docID = DocIdSetIterator.NO_MORE_DOCS;
        }
        return docID == target;
    }

    @Override
    public int docID() {
        return docID;
    }

    @Override
    public int nextDoc() {
        docID++;

        if (!isValidDocID(docID)) {
            docID = DocIdSetIterator.NO_MORE_DOCS;
        }

        return docID;
    }

    @Override
    public int advance(int target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long cost() {
        return numDocs;
    }
}
