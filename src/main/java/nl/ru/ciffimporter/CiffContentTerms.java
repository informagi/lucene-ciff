package nl.ru.ciffimporter;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.SortedMap;
import java.util.TreeMap;

import static io.osirrc.ciff.CommonIndexFileFormat.Header;
import static io.osirrc.ciff.CommonIndexFileFormat.PostingsList;

public class CiffContentTerms extends Terms {

    private final Header header;
    private final SortedMap<BytesRef, PostingsList> postingsLists;

    public CiffContentTerms(Header header, PostingsList[] postingsLists) {
        this.header = header;

        this.postingsLists = new TreeMap<>();
        for (PostingsList postingsList : postingsLists) {
            this.postingsLists.put(new BytesRef(postingsList.getTerm()), postingsList);
        }
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
        long total = 0;
        for (PostingsList postingsList : postingsLists.values()) {
            total += postingsList.getCf();
        }
        return total;
    }

    @Override
    public long getSumDocFreq() {
        long total = 0;
        for (PostingsList postingsList : postingsLists.values()) {
            total += postingsList.getDf();
        }
        return total;
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