package nl.ru.ciffimporter;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Iterator;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffDocValues extends BinaryDocValues {

    private final CiffReader reader;
    private final Iterator<DocRecord> docRecords;

    private int docID = -1;
    private String collectionDocId = "";

    public CiffDocValues(String input) throws IOException {
        this.reader = new CiffReader(input, true);
        this.docRecords = reader.getDocRecords();
    }

    @Override
    public BytesRef binaryValue() {
        return new BytesRef(collectionDocId);
    }

    @Override
    public boolean advanceExact(int target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int docID() {
        return docID;
    }

    @Override
    public int nextDoc() {
        if (this.docRecords.hasNext()) {
            DocRecord doc = this.docRecords.next();
            this.docID = doc.getDocid();
            this.collectionDocId = doc.getCollectionDocid();
        } else {
            this.docID = DocIdSetIterator.NO_MORE_DOCS;
            this.collectionDocId = null;

            try {
                this.reader.close();
            } catch (IOException ignored) {}
        }

        return this.docID();
    }

    @Override
    public int advance(int target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long cost() {
        return this.reader.getHeader().getNumDocs();
    }
}
