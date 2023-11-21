package nl.ru.ciffimporter;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

import java.util.Iterator;

import static io.osirrc.ciff.CommonIndexFileFormat.Header;
import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffIdTerms extends Terms {

    private final Header header;
    private final Iterator<DocRecord> docRecords;

    public CiffIdTerms(Header header, Iterator<DocRecord> docRecords) {
        this.header = header;
        this.docRecords = docRecords;
    }

    @Override
    public TermsEnum iterator() {
        return new CiffIdTermsEnum(docRecords);
    }

    @Override
    public long size() {
        return header.getNumDocs();
    }

    @Override
    public long getSumTotalTermFreq() {
        throw new UnsupportedOperationException();
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
        return false;
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