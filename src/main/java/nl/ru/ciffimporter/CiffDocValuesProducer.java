package nl.ru.ciffimporter;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.EmptyDocValuesProducer;
import org.apache.lucene.index.FieldInfo;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffDocValuesProducer extends EmptyDocValuesProducer {

    private final DocRecord[] docRecords;

    public CiffDocValuesProducer(DocRecord[] docRecords) {
        this.docRecords = docRecords;
    }

    @Override
    public BinaryDocValues getBinary(FieldInfo field) {
        if (field.name.equals("id")) {
            return new CiffDocValues(docRecords);
        }
        throw new IllegalArgumentException("Field '" + field + "' is not indexed in CIFF.");
    }
}