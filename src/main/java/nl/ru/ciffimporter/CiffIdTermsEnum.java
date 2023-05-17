package nl.ru.ciffimporter;

import org.apache.lucene.index.BaseTermsEnum;
import org.apache.lucene.index.ImpactsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;


public class CiffIdTermsEnum extends BaseTermsEnum {

    private final SortedMap<BytesRef, DocRecord> docRecords;
    private Iterator<Map.Entry<BytesRef, DocRecord>> iterator;
    private Map.Entry<BytesRef, DocRecord> current;

    public CiffIdTermsEnum(SortedMap<BytesRef, DocRecord> docRecords) {
        this.docRecords = docRecords;
        this.iterator = docRecords.entrySet().iterator();
    }

    @Override
    public SeekStatus seekCeil(BytesRef text) {
        SortedMap<BytesRef, DocRecord> tailMap = docRecords.tailMap(text);
        if (tailMap.isEmpty()) {
            return SeekStatus.END;
        } else {
            iterator = tailMap.entrySet().iterator();
            current = iterator.next();

            if (current.getKey().equals(text)) {
                return SeekStatus.FOUND;
            } else {
                return SeekStatus.NOT_FOUND;
            }
        }
    }

    @Override
    public void seekExact(long ord) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BytesRef term() {
        return current.getKey();
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
        return new CiffIdPostingsEnum(current.getValue());
    }

    @Override
    public ImpactsEnum impacts(int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BytesRef next() {
        if (iterator.hasNext()) {
            current = iterator.next();
            return term();
        }
        return null;
    }
}
