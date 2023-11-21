package nl.ru.ciffimporter;

import static io.osirrc.ciff.CommonIndexFileFormat.PostingsList;
import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CiffFields extends Fields implements Closeable {

    private final List<String> FIELDS = List.of(CiffImporter.CONTENTS, CiffImporter.ID);

    private final CiffReader reader;
    private final Iterator<PostingsList> postingsLists;
    private final Iterator<DocRecord> docRecords;

    public CiffFields(String input) throws IOException {
        this.reader = new CiffReader(input);
        this.postingsLists = reader.getPostingsLists();
        this.docRecords = reader.getDocRecords();
    }

    @Override
    public Iterator<String> iterator() {
        return FIELDS.iterator();
    }

    @Override
    public Terms terms(String field) {
        if (field.equals(CiffImporter.CONTENTS)) {
            return new CiffContentTerms(reader.getHeader(), postingsLists);
        } else if (field.equals(CiffImporter.ID)) {
            return new CiffIdTerms(reader.getHeader(), docRecords);
        }
        throw new IllegalArgumentException("Field '" + field + "' is not indexed in CIFF.");
    }

    @Override
    public int size() {
        return FIELDS.size();
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }
}
