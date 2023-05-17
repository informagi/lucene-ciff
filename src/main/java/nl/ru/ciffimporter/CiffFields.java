package nl.ru.ciffimporter;

import static io.osirrc.ciff.CommonIndexFileFormat.Header;
import static io.osirrc.ciff.CommonIndexFileFormat.PostingsList;
import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;

import java.util.Iterator;
import java.util.List;

public class CiffFields extends Fields {

    private final Header header;
    private final PostingsList[] postingsLists;
    private final DocRecord[] docRecords;

    public CiffFields(Header header, PostingsList[] postingsLists, DocRecord[] docRecords) {
        this.header = header;
        this.postingsLists = postingsLists;
        this.docRecords = docRecords;
    }

    @Override
    public Iterator<String> iterator() {
        List<String> fields = List.of(CiffImporter.CONTENTS, CiffImporter.ID);
        return fields.iterator();
    }

    @Override
    public Terms terms(String field) {
        if (field.equals(CiffImporter.CONTENTS)) {
            return new CiffContentTerms(header, postingsLists);
        } else if (field.equals(CiffImporter.ID)) {
            return new CiffIdTerms(header, docRecords);
        }
        throw new IllegalArgumentException("Field '" + field + "' is not indexed in CIFF.");
    }

    @Override
    public int size() {
        return 1;
    }
}
