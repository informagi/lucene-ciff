package nl.ru.ciffimporter;

import org.apache.lucene.codecs.NormsProducer;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.NumericDocValues;

import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffNormsProducer extends NormsProducer {

    private final DocRecord[] docRecords;

    public CiffNormsProducer(DocRecord[] docRecords) {
        this.docRecords = docRecords;
    }

    @Override
    public NumericDocValues getNorms(FieldInfo field) {
        if (field.name.equals(CiffImporter.CONTENTS)) {
            return new CiffNorms(docRecords);
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