package messaging;

import chunkserver.Chunk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class ChunkReadResponse extends ChunkMessage {

    // In-memory Chunk representation containing metadata and integrity information
    public Chunk chunk;

    // List of Chunk Server hostnames a replacement/correction had to be made on due to failed integrity check
    public List<String> chunkReplacements;

    public ChunkReadResponse(String hostname, String ipAddress, Integer port, String absoluteFilePath, Integer sequence,
                             Chunk chunk, List<String> chunkReplacements) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.absoluteFilePath = absoluteFilePath;
        this.sequence = sequence;
        this.chunk = chunk;
        this.chunkReplacements = chunkReplacements;

        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal ChunkReadResponse: {}", e.getMessage());
        }
    }

    public ChunkReadResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.CHUNK_READ_RESPONSE;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public List<String> getChunkReplacements() {
        return chunkReplacements;
    }

    /**
     * In addition to the header, chunk filename, and sequence, writes the Chunk object, and replacements made.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @throws IOException If fails to write to DataOutputStream
     */
    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream); // first marshal common Message header
        writeChunk(dataOutputStream, this.chunk);
        writeStringList(dataOutputStream, this.chunkReplacements);
    }

    /**
     * In addition to the header, chunk filename, and sequence, reads the Chunk object, and replacements made.
     * @param dataInputStream The DataInputStream we are reading from.
     * @throws IOException If fails to read from DataInputStream
     */
    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream); // first unmarshal common Message header
        this.chunk = readChunk(dataInputStream);
        this.chunkReplacements = readStringList(dataInputStream);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof ChunkReadResponse)) return false;
        ChunkReadResponse crrOther = (ChunkReadResponse) other;
        return this.absoluteFilePath.equals(crrOther.getAbsoluteFilePath()) &&
                this.sequence.equals(crrOther.getSequence());
    }

    @Override
    public String toString() {
        return "> ChunkReadResponse:\n" +
                String.format("  absoluteFilePath: %s\n", this.absoluteFilePath) +
                String.format("  sequence: %d\n", this.sequence) +
                String.format("  chunk: %s\n", this.chunk) +
                String.format("  chunkReplacements: %b\n", this.chunkReplacements);
    }
}
