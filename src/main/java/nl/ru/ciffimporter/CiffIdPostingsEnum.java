package nl.ru.ciffimporter;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffIdPostingsEnum extends PostingsEnum {

    private final DocRecord docRecord;
    private int docID = -1;

    public CiffIdPostingsEnum(DocRecord docRecord) {
        this.docRecord = docRecord;
    }

    @Override
    public int freq() {
        // We do not index frequencies for document identifiers
        return -1;
    }

    @Override
    public int nextPosition() {
        // We do not index positions
        return -1;
    }

    @Override
    public int startOffset() {
        // We do not index offsets
        return -1;
    }

    @Override
    public int endOffset() {
        // We do not index offsets
        return -1;
    }

    @Override
    public BytesRef getPayload() {
        // We do not index payloads
        return null;
    }

    @Override
    public int docID() {
        return docID;
    }

    @Override
    public int nextDoc() {
        if (docID >= 0) {
            return docID = PostingsEnum.NO_MORE_DOCS;
        }

        docID = docRecord.getDocid();

        return docID;
    }

    @Override
    public int advance(int target) {
        int doc = nextDoc();
        while (doc < target) {
            doc = nextDoc();
        }
        return doc;
    }

    @Override
    public long cost() {
        return 1;
    }
}