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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static io.osirrc.ciff.CommonIndexFileFormat.Header;
import static io.osirrc.ciff.CommonIndexFileFormat.PostingsList;
import static io.osirrc.ciff.CommonIndexFileFormat.DocRecord;

public class CiffImporter {

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
            false,
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

        Header ciffHeader;
        PostingsList[] postingsLists;
        DocRecord[] docRecords;

        try (InputStream inputStream = new FileInputStream(inputFile)) {
            ciffHeader = Header.parseDelimitedFrom(inputStream);

            postingsLists = new PostingsList[ciffHeader.getNumPostingsLists()];
            for (int i = 0; i < ciffHeader.getNumPostingsLists(); i++) {
                postingsLists[i] = PostingsList.parseDelimitedFrom(inputStream);
            }

            docRecords = new DocRecord[ciffHeader.getNumDocs()];
            for (int i = 0; i < ciffHeader.getNumDocs(); i++) {
                docRecords[i] = DocRecord.parseDelimitedFrom(inputStream);
            }
        } catch (IOException e) {
            System.err.println("Exception while reading '" + inputFile + "'");
            return;
        }

        new CiffImporter(ciffHeader, postingsLists, docRecords, codec, outputDirectory).importCiff();
    }

    private final Header header;
    private final PostingsList[] postingsLists;
    private final DocRecord[] docRecords;

    private final Codec codec;
    private TrackingDirectoryWrapper outputDirectory;
    private SegmentInfo segmentInfo;
    private IOContext writeContext;
    private SegmentWriteState writeState;

    public CiffImporter(Header header, PostingsList[] postingsLists, DocRecord[] docRecords, Codec codec, String outputDirectory) {
        this.header = header;
        this.postingsLists = postingsLists;
        this.docRecords = docRecords;
        this.codec = codec;

        try {
            Path indexPath = Files.createDirectory(Paths.get(outputDirectory));
            this.outputDirectory = new TrackingDirectoryWrapper(FSDirectory.open(indexPath));
        } catch (IOException e) {
            System.err.println("Could not create directory '" + outputDirectory + '"');
            e.printStackTrace();

            return;
        }

        this.writeContext = new IOContext(new FlushInfo(header.getNumDocs(), -1));

        this.segmentInfo = new SegmentInfo(
            this.outputDirectory,
            Version.LATEST,
            Version.LATEST,
            "_0",
            header.getNumDocs(),
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
        writePostings();
        writeStoredFields();
        writeDocValues();
        writeNorms();
        writeFieldInfos();
        writeSegmentInfo();

        System.out.println("CIFF IMPORT: generated the following Lucene files:");
        for (String file : outputDirectory.listAll()) {
            System.out.println("  " + file + ": " + outputDirectory.fileLength(file) + " bytes");
        }
    }

    private void writePostings() throws IOException {
        Fields fields = new CiffFields(header, postingsLists, docRecords);

        try (FieldsConsumer fieldsConsumer = codec.postingsFormat().fieldsConsumer(writeState);
                NormsProducer normsProducer = new CiffNormsProducer(docRecords)) {
            fieldsConsumer.write(fields, normsProducer);
        }
    }

    private void writeStoredFields() throws IOException {
        StoredFieldsWriter writer = codec.storedFieldsFormat().fieldsWriter(outputDirectory, segmentInfo, writeContext);

        for (DocRecord docRecord : docRecords) {
            writer.startDocument();
            writer.writeField(ID_FIELD_INFO, new StringField(ID, docRecord.getCollectionDocid(), Field.Store.YES));
            writer.finishDocument();
        }

        writer.finish(header.getNumDocs());
        writer.close();
    }

    private void writeDocValues() throws IOException {
        DocValuesConsumer consumer = codec.docValuesFormat().fieldsConsumer(writeState);
        DocValuesProducer ciffValuesProducer = new CiffDocValuesProducer(docRecords);
        consumer.addBinaryField(ID_FIELD_INFO, ciffValuesProducer);
        consumer.close();
    }

    private void writeNorms() throws IOException {
        NormsConsumer normsConsumer = codec.normsFormat().normsConsumer(writeState);
        CiffNormsProducer ciffNormsProducer = new CiffNormsProducer(docRecords);
        normsConsumer.addNormsField(CONTENTS_FIELD_INFO, ciffNormsProducer);
        normsConsumer.close();
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
}
