package nl.ru.ciffimporter;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.EmptyDocValuesProducer;
import org.apache.lucene.index.FieldInfo;

import java.io.IOException;

public class CiffDocValuesProducer extends EmptyDocValuesProducer {

    private final String input;

    public CiffDocValuesProducer(String input) {
        this.input = input;
    }

    @Override
    public BinaryDocValues getBinary(FieldInfo field) throws IOException {
        if (field.name.equals(CiffImporter.ID)) {
            return new CiffDocValues(input);
        }
        throw new IllegalArgumentException("Field '" + field + "' is not indexed in CIFF.");
    }
}
