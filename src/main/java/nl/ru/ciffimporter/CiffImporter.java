package nl.ru.ciffimporter;

import org.apache.lucene.codecs.*;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.FlushInfo;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.TrackingDirectoryWrapper;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.osirrc.ciff.CommonIndexFileFormat.Header;
import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffImporter {

    private static final int NUM_TASKS = 6;

    public static final String ID = "id";
    public static final String CONTENTS = "contents";

    private static final int ID_FIELD_NUM = 0;
    private static final int CONTENTS_FIELD_NUM = 1;

    private static final FieldInfo ID_FIELD_INFO = new FieldInfo(
            ID,
            ID_FIELD_NUM,
            false,
            true,
            false,
            IndexOptions.DOCS,
            DocValuesType.BINARY,
            -1,
            new HashMap<>(),
            0,
            0,
            0,
            0,
            VectorEncoding.FLOAT32,
            VectorSimilarityFunction.EUCLIDEAN,
            false
    );

    private static final FieldInfo CONTENTS_FIELD_INFO = new FieldInfo(
            CONTENTS,
            CONTENTS_FIELD_NUM,
            false,
            true,
            false,
            IndexOptions.DOCS_AND_FREQS,
            DocValuesType.NONE,
            -1,
            new HashMap<>(),
            0,
            0,
            0,
            0,
            VectorEncoding.FLOAT32,
            VectorSimilarityFunction.EUCLIDEAN,
            false
    );

    private static final FieldInfos FIELD_INFOS = new FieldInfos(new FieldInfo[] { ID_FIELD_INFO, CONTENTS_FIELD_INFO });

    public static void main(String[] args)
            throws IOException {
        String inputFile;
        String outputDirectory;
        Codec codec;

        if (args.length == 3) {
            inputFile = args[0];
            outputDirectory = args[1];
            codec = Codec.forName(args[2]);
        } else if (args.length == 2) {
            inputFile = args[0];
            outputDirectory = args[1];
            codec = Codec.getDefault();
        } else {
            throw new IllegalArgumentException("Usage: CiffImporter INPUT OUTPUT [CODEC]");
        }

        new CiffImporter(inputFile, outputDirectory, codec).importCiff();
    }

    private final String inputFile;
    private final Codec codec;
    private final TrackingDirectoryWrapper outputDirectory;
    private final SegmentInfo segmentInfo;
    private final IOContext writeContext;
    private final SegmentWriteState writeState;

    private final Header header;

    public CiffImporter(String inputFile, String outputDirectory, Codec codec) throws IOException {
        this.inputFile = inputFile;
        this.codec = codec;

        Path indexPath = Files.createDirectory(Paths.get(outputDirectory));
        this.outputDirectory = new TrackingDirectoryWrapper(FSDirectory.open(indexPath));

        try (CiffReader reader = new CiffReader(this.inputFile)) {
            this.header = reader.getHeader();
        }

        this.writeContext = new IOContext(new FlushInfo(this.header.getNumDocs(), -1));

        this.segmentInfo = new SegmentInfo(
            this.outputDirectory,
            Version.LATEST,
            Version.LATEST,
            "_0",
            this.header.getNumDocs(),
            false,
            codec,
            Collections.emptyMap(),
            StringHelper.randomId(),
            Collections.emptyMap(),
            null
        );

        this.writeState = new SegmentWriteState(
            null,
            this.outputDirectory,
            segmentInfo,
            FIELD_INFOS,
            null,
            this.writeContext
        );
    }

    public void importCiff() throws IOException {
        runWithLogging(this::writePostings, 1, "postings");
        runWithLogging(this::writeStoredFields, 2, "stored fields");
        runWithLogging(this::writeDocValues, 3, "doc values");
        runWithLogging(this::writeNorms, 4, "norms");
        runWithLogging(this::writeFieldInfos, 5, "field infos");
        runWithLogging(this::writeSegmentInfo, 6, "segment info");

        System.err.println("CIFF import complete!");
        System.err.println("Generated the following Lucene files:");
        for (String file : outputDirectory.listAll()) {
            System.err.println("  " + file + ": " + outputDirectory.fileLength(file) + " bytes");
        }
    }

    private void runWithLogging(ImportTask r, int taskNum, String taskName) throws IOException {
        System.err.printf("[%d/%d] Writing %s", taskNum, NUM_TASKS, taskName);
        System.err.flush();

        long start = System.currentTimeMillis();
        r.run();
        long end = System.currentTimeMillis();

        System.err.printf(" (%.1fs)\n", (end - start) / 1000F);
        System.err.flush();
    }

    private void writePostings() throws IOException {
        try (FieldsConsumer fieldsConsumer = codec.postingsFormat().fieldsConsumer(writeState);
                CiffFields fields = new CiffFields(this.inputFile)) {
            fieldsConsumer.write(fields, null);
        }
    }

    private void writeStoredFields() throws IOException {
        try (CiffReader reader = new CiffReader(this.inputFile, true);
                StoredFieldsWriter writer = codec.storedFieldsFormat().fieldsWriter(outputDirectory, segmentInfo, writeContext)) {
            Iterator<DocRecord> docRecords = reader.getDocRecords();
            while (docRecords.hasNext()) {
                DocRecord doc = docRecords.next();

                writer.startDocument();
                writer.writeField(ID_FIELD_INFO, new StringField(ID, doc.getCollectionDocid(), Field.Store.YES));
                writer.finishDocument();
            }

            writer.finish(header.getNumDocs());
        }
    }

    private void writeDocValues() throws IOException {
        try (DocValuesConsumer consumer = codec.docValuesFormat().fieldsConsumer(writeState)) {
            DocValuesProducer ciffValuesProducer = new CiffDocValuesProducer(this.inputFile);
            consumer.addBinaryField(ID_FIELD_INFO, ciffValuesProducer);
        }
    }

    private void writeNorms() throws IOException {
        try (NormsConsumer normsConsumer = codec.normsFormat().normsConsumer(writeState);
                CiffNormsProducer ciffNormsProducer = new CiffNormsProducer(this.inputFile)) {
            normsConsumer.addNormsField(CONTENTS_FIELD_INFO, ciffNormsProducer);
        }
    }

    private void writeFieldInfos() throws IOException {
        codec.fieldInfosFormat().write(outputDirectory, segmentInfo, "", FIELD_INFOS, writeContext);
    }

    private void writeSegmentInfo() throws IOException {
        segmentInfo.setFiles(new HashSet<>(outputDirectory.getCreatedFiles()));

        codec.segmentInfoFormat().write(outputDirectory, segmentInfo, writeContext);

        SegmentInfos segmentInfos = new SegmentInfos(Version.LATEST.major);
        SegmentCommitInfo segmentCommitInfo = new SegmentCommitInfo(segmentInfo, 0, 0, -1, -1, -1, StringHelper.randomId());
        segmentInfos.add(segmentCommitInfo);
        segmentInfos.commit(outputDirectory);
    }

    private interface ImportTask {
        void run() throws IOException;
    }
}
