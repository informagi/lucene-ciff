package nl.ru.ciffimporter;

import org.apache.lucene.index.BaseTermsEnum;
import org.apache.lucene.index.ImpactsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

import static io.osirrc.ciff.CommonIndexFileFormat.PostingsList;


public class CiffContentTermsEnum extends BaseTermsEnum {

    private final SortedMap<BytesRef, PostingsList> postingsLists;
    private Iterator<Map.Entry<BytesRef, PostingsList>> iterator;
    private Map.Entry<BytesRef, PostingsList> current;

    public CiffContentTermsEnum(SortedMap<BytesRef, PostingsList> postingsLists) {
        this.postingsLists = postingsLists;
        this.iterator = postingsLists.entrySet().iterator();
    }

    @Override
    public SeekStatus seekCeil(BytesRef text) {
        SortedMap<BytesRef, PostingsList> tailMap = postingsLists.tailMap(text);
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
        return (int) current.getValue().getDf();
    }

    @Override
    public long totalTermFreq() {
        return current.getValue().getCf();
    }

    @Override
    public PostingsEnum postings(PostingsEnum reuse, int flags) {
        return new CiffContentPostingsEnum(current.getValue().getPostingsList());
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
