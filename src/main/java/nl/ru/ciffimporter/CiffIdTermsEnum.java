package nl.ru.ciffimporter;

import org.apache.lucene.index.BaseTermsEnum;
import org.apache.lucene.index.ImpactsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.Iterator;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;


public class CiffIdTermsEnum extends BaseTermsEnum {

    private final Iterator<DocRecord> docRecords;
    private DocRecord currentDoc;

    public CiffIdTermsEnum(Iterator<DocRecord> docRecords) {
        this.docRecords = docRecords;
    }

    @Override
    public SeekStatus seekCeil(BytesRef text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void seekExact(long ord) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BytesRef term() {
        return new BytesRef(currentDoc.getCollectionDocid());
    }

    @Override
    public long ord() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int docFreq() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long totalTermFreq() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PostingsEnum postings(PostingsEnum reuse, int flags) {
        return new CiffIdPostingsEnum(currentDoc);
    }

    @Override
    public ImpactsEnum impacts(int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BytesRef next() {
        // TODO: this currently skips records with duplicate document identifiers. Ideally, each document identifier
        //  only occurs once per CIFF file, so we might need to revisit this later.
        while (docRecords.hasNext()) {
            final DocRecord newRecord = docRecords.next();

            if (currentDoc == null) {
                currentDoc = newRecord;
                return term();
            }

            final BytesRef prevTerm = term();
            currentDoc = newRecord;
            final BytesRef newTerm = term();

            if (!prevTerm.bytesEquals(newTerm)) {
                return newTerm;
            }
        }
        return null;
    }
}
