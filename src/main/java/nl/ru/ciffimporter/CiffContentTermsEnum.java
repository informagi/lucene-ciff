package nl.ru.ciffimporter;

import org.apache.lucene.index.BaseTermsEnum;
import org.apache.lucene.index.ImpactsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.Iterator;

import static io.osirrc.ciff.CommonIndexFileFormat.PostingsList;


public class CiffContentTermsEnum extends BaseTermsEnum {

    private final Iterator<PostingsList> postingsLists;
    private PostingsList currentPostingsList;

    public CiffContentTermsEnum(Iterator<PostingsList> postingsLists) {
        this.postingsLists = postingsLists;
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
        return new BytesRef(currentPostingsList.getTerm());
    }

    @Override
    public long ord() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int docFreq() {
        return (int) currentPostingsList.getDf();
    }

    @Override
    public long totalTermFreq() {
        return currentPostingsList.getCf();
    }

    @Override
    public PostingsEnum postings(PostingsEnum reuse, int flags) {
        return new CiffContentPostingsEnum(currentPostingsList.getPostingsList());
    }

    @Override
    public ImpactsEnum impacts(int flags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BytesRef next() {
        if (postingsLists.hasNext()) {
            currentPostingsList = postingsLists.next();
            return term();
        }
        return null;
    }
}
