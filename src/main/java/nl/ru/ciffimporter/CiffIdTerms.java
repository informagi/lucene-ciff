package nl.ru.ciffimporter;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.SortedMap;
import java.util.TreeMap;

import static io.osirrc.ciff.CommonIndexFileFormat.Header;
import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffIdTerms extends Terms {

    private final Header header;
    private final SortedMap<BytesRef, DocRecord> docRecords;

    public CiffIdTerms(Header header, DocRecord[] docRecords) {
        this.header = header;

        this.docRecords = new TreeMap<>();
        for (DocRecord docRecord : docRecords) {
            this.docRecords.put(new BytesRef(docRecord.getCollectionDocid()), docRecord);
        }
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