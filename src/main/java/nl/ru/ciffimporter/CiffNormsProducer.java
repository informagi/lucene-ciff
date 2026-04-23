package nl.ru.ciffimporter;

import org.apache.lucene.codecs.NormsProducer;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.NumericDocValues;

import java.io.IOException;

public class CiffNormsProducer extends NormsProducer {

    private final String input;
    private CiffNorms norms;

    public CiffNormsProducer(String input) {
        this.input = input;
    }

    @Override
    public NumericDocValues getNorms(FieldInfo field) throws IOException {
        if (field.name.equals(CiffImporter.CONTENTS)) {
            if (norms == null) {
                norms = new CiffNorms(input);
            }
            norms.reset();
            return norms;
        }
        throw new IllegalArgumentException("Field '" + field + "' is not indexed in CIFF.");
    }

    @Override
    public void checkIntegrity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {}
}