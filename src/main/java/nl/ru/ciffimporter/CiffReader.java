package nl.ru.ciffimporter;

import com.google.protobuf.Message;

import java.io.*;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import static io.osirrc.ciff.CommonIndexFileFormat.*;

public class CiffReader implements Closeable {

    private final InputStream inputStream;
    private final Header header;

    public CiffReader(String inputFile) throws IOException {
        this(inputFile, false);
    }

    public CiffReader(String inputFile, boolean skipPostingsLists) throws IOException {
        if (inputFile.endsWith(".gz")) {
            this.inputStream = new GZIPInputStream(new FileInputStream(inputFile));
        } else {
            this.inputStream = new FileInputStream(inputFile);
        }

        this.header = Header.parseDelimitedFrom(this.inputStream);
        if (skipPostingsLists) {
            this.getPostingsLists().exhaust();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.inputStream != null) {
            this.inputStream.close();
        }
    }

    public Header getHeader() {
        return this.header;
    }

    public MessageIterator<PostingsList> getPostingsLists() {
        return new PostingsListIterator(this.inputStream, this.header.getNumPostingsLists());
    }

    public MessageIterator<DocRecord> getDocRecords() {
        return new DocRecordIterator(this.inputStream, this.header.getNumDocs());
    }

    public abstract static class MessageIterator<M extends Message> implements Iterator<M> {

        protected final InputStream in;

        private final int numMessages;
        private int numRead = 0;

        private MessageIterator(InputStream in, int numMessages) {
            this.in = in;
            this.numMessages = numMessages;
        }

        @Override
        public boolean hasNext() {
            return this.numRead < this.numMessages;
        }

        @Override
        public M next() {
            try {
                M m = this.readNext();
                this.numRead++;
                return m;
            } catch (IOException e) {
                return null;
            }
        }

        public void exhaust() {
            while (this.hasNext()) {
                this.next();
            }
        }

        protected abstract M readNext() throws IOException;
    }

    private class PostingsListIterator extends MessageIterator<PostingsList> {

        private PostingsListIterator(InputStream in, int numPostingsLists) {
            super(in, numPostingsLists);
        }

        @Override
        protected PostingsList readNext() throws IOException {
            return PostingsList.parseDelimitedFrom(this.in);
        }
    }

    private class DocRecordIterator extends MessageIterator<DocRecord> {

        private DocRecordIterator(InputStream in, int numDocRecords) {
            super(in, numDocRecords);
        }

        @Override
        protected DocRecord readNext() throws IOException {
            return DocRecord.parseDelimitedFrom(this.in);
        }
    }

}
