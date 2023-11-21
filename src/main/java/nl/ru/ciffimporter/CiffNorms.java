package nl.ru.ciffimporter;

import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.SmallFloat;

import java.io.IOException;
import java.util.Iterator;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffNorms extends NumericDocValues {

    private final CiffReader reader;
    private final Iterator<DocRecord> docRecords;

    private int docID = -1;
    private int length = -1;

    public CiffNorms(String input) throws IOException {
        this.reader = new CiffReader(input, true);
        this.docRecords = reader.getDocRecords();
    }

    @Override
    public long longValue() {
        return SmallFloat.intToByte4(length);
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
            this.length = doc.getDoclength();
        } else {
            this.docID = DocIdSetIterator.NO_MORE_DOCS;
            this.length = -1;

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
