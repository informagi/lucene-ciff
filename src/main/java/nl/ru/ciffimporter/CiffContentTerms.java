package nl.ru.ciffimporter;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

import java.util.Iterator;

import static io.osirrc.ciff.CommonIndexFileFormat.Header;
import static io.osirrc.ciff.CommonIndexFileFormat.PostingsList;

public class CiffContentTerms extends Terms {

    private final Header header;
    private final Iterator<PostingsList> postingsLists;

    public CiffContentTerms(Header header, Iterator<PostingsList> postingsLists) {
        this.header = header;
        this.postingsLists = postingsLists;
    }

    @Override
    public TermsEnum iterator() {
        return new CiffContentTermsEnum(postingsLists);
    }

    @Override
    public long size() {
        return header.getNumPostingsLists();
    }

    @Override
    public long getSumTotalTermFreq() {
        return this.header.getTotalTermsInCollection();
    }

    @Override
    public long getSumDocFreq() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDocCount() {
        // TODO: header.num_docs currently also contains the empty documents, so we cannot use it here
        //  if the index contains empty documents as well.
        return header.getNumDocs();
    }

    @Override
    public boolean hasFreqs() {
        return true;
    }

    @Override
    public boolean hasOffsets() {
        return false;
    }

    @Override
    public boolean hasPositions() {
        return false;
    }

    @Override
    public boolean hasPayloads() {
        return false;
    }
}