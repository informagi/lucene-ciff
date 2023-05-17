package nl.ru.ciffimporter;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.Iterator;
import java.util.List;

import static io.osirrc.ciff.CommonIndexFileFormat.Posting;

public class CiffContentPostingsEnum extends PostingsEnum {

    private final Iterator<Posting> postingsIterator;
    private final int numPostings;
    private int docID = -1;
    private int freq;

    public CiffContentPostingsEnum(List<Posting> postings) {
        this.postingsIterator = postings.iterator();
        this.numPostings = postings.size();
    }

    @Override
    public int freq() {
        return freq;
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
        if (!postingsIterator.hasNext()) {
            return docID = PostingsEnum.NO_MORE_DOCS;
        }

        Posting posting = postingsIterator.next();

        docID = docID == -1 ? posting.getDocid() : docID + posting.getDocid();
        freq = posting.getTf();

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
        return numPostings;
    }
}