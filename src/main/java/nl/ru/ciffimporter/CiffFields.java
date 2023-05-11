package nl.ru.ciffimporter;

import static io.osirrc.ciff.CommonIndexFileFormat.Header;
import static io.osirrc.ciff.CommonIndexFileFormat.PostingsList;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;

import java.util.Iterator;
import java.util.List;

public class CiffFields extends Fields {

    private final Header header;
    private final PostingsList[] postingsLists;

    public CiffFields(Header header, PostingsList[] postingsLists) {
        this.header = header;
        this.postingsLists = postingsLists;
    }

    @Override
    public Iterator<String> iterator() {
        List<String> fields = List.of(CiffImporter.CONTENTS);
        return fields.iterator();
    }

    @Override
    public Terms terms(String field) {
        if (field.equals(CiffImporter.CONTENTS)) {
            return new CiffTerms(header, postingsLists);
        }
        throw new IllegalArgumentException("Field '" + field + "' is not indexed in CIFF.");
    }

    @Override
    public int size() {
        return 1;
    }
}
