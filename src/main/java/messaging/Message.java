package messaging;

import chunkserver.Chunk;
import chunkserver.ChunkIntegrity;
import chunkserver.ChunkMetadata;
import controller.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Host;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.*;

public abstract class Message {

    public static Logger log = LoggerFactory.getLogger(Message.class);

    public enum MessageType {
        HEARTBEAT_MINOR, HEARTBEAT_MAJOR, CHUNK_STORE_REQUEST, CHUNK_STORE_RESPONSE, CLIENT_WRITE_REQUEST, CLIENT_WRITE_RESPONSE,
         CLIENT_READ_REQUEST, CLIENT_READ_RESPONSE, CHUNK_READ_REQUEST, CHUNK_READ_RESPONSE, CHUNK_REPLACEMENT_REQUEST,
        CHUNK_REPLACEMENT_RESPONSE, CHUNK_REPLICATION_INFO, CHUNK_CORRECTION_NOTIFICATION, CHUNK_REPLICATE_COMMAND,
        SYSTEM_REPORT_REQUEST, SYSTEM_REPORT_RESPONSE
    }

    public String hostname, ipAddress;
    public Integer port;
    public byte[] marshaledBytes;

    // --- Getters ---

    public abstract MessageType getType();

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public byte[] getMarshaledBytes() {
        return marshaledBytes;
    }

    // --- Common message utility functions ---

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Message)) return false;
        Message otherMessage = (Message) other;
        return (this.getType().equals(otherMessage.getType()) &&
                this.getHostname().equals(otherMessage.getHostname()) &&
                this.getIpAddress().equals(otherMessage.getIpAddress()) &&
                this.getPort().equals(otherMessage.getPort())
        );
    }

    /**
     * Marshals/packs the object header fields into the message's byte array representation.
     * This is a partial implementation of the full marshaling process; subclasses are expected to complete this.
     * The message header is represented as follows:
     * - message type (int 4 bytes)
     * - hostname length (int 4 bytes)
     * - hostname string (char[] n bytes)
     * - ip length (int 4 bytes)
     * - ip string (char[] n bytes)
     * - port (int 4 bytes)
     * @param dataOutputStream The DataOutputStream we are writing to.
     */
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(integerFromType(this.getType()));
        writeString(dataOutputStream, this.hostname);
        writeString(dataOutputStream, this.ipAddress);
        dataOutputStream.writeInt(this.port);
    }

    /**
     * Marshals all the fields of this object into the byte array field.
     * @throws IOException If unable to write to the output stream
     */
    public void marshal() throws IOException {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(byteOutStream));
        marshal(dataOutStream);
        collectByteStream(dataOutStream, byteOutStream);
        dataOutStream.close();
        byteOutStream.close();
    }

    /**
     * Unmarshals/unpacks the header fields from the message's byte array into the instance variables.
     * The message header is represented as follows:
     * - hostname length (int 4 bytes)
     * - hostname string (char[] n bytes)
     * - ip length (int 4 bytes)
     * - ip string (char[] n bytes)
     * - port (int 4 bytes)
     * @throws IOException If fails to read from DataInputStream
     */
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        this.hostname = readString(dataInputStream);
        this.ipAddress = readString(dataInputStream);
        this.port = dataInputStream.readInt();
    }

    /**
     * Completes the marshaling process by flushing the DataOutputStream to the ByteArrayOutputStream, then
     * collecting as a byte array into the object's marshalledBytes field. Lastly, the streams are closed.
     * @param dataOutStream The DataOutputStream we have been writing to.
     * @param byteOutStream The ByteArrayOutputStream we have been flushing bytes to.
     * @throws IOException If fails to write to DataOutputStream
     */
    public void collectByteStream(DataOutputStream dataOutStream, ByteArrayOutputStream byteOutStream)
            throws IOException {
        dataOutStream.flush();
        this.marshaledBytes = byteOutStream.toByteArray();
    }

    // --- Static helper functions ---

    /**
     * Writes an integer to the output stream.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @param value The int we are writing.
     * @throws IOException If fails to write to the DataOutputStream
     */
    public static void writeInt(DataOutputStream dataOutputStream, int value) throws IOException {
        dataOutputStream.writeInt(value);
    }

    /**
     * Reads and returns an integer to the input stream.
     * @param dataInputStream The DataInputStream we are reading from.
     * @return The int we read.
     * @throws IOException If fails to read from the DataInputStream
     */
    public static int readInt(DataInputStream dataInputStream) throws IOException {
        return dataInputStream.readInt();
    }

    /**
     * Reads a string from the DataInputStream passed in as follows:
     * 1. Reads the string length as an integer.
     * 2. Reads the string bytes; creates and returns a string from said bytes.
     * @param dataInputStream The DataInputStream containing the bytes we are reading.
     * @return The String, whose length is specified before the string bytes.
     * @throws IOException If fails to read from DataInputStream
     */
    public static String readString(DataInputStream dataInputStream) throws IOException {
        int stringLength = dataInputStream.readInt();
        byte[] stringBytes = new byte[stringLength];
        dataInputStream.readFully(stringBytes, 0, stringLength);
        return new String(stringBytes);
    }

    /**
     * Writes a string to the DataOutputString passed in as follows:
     * 1. Writes the string length as an integer
     * 2. Writes the string bytes
     * @param dataOutputStream DataOutputStream containing the byte array we are writing to
     * @param value The String value we are writing to the byte array
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeString(DataOutputStream dataOutputStream, String value) throws IOException {
        dataOutputStream.writeInt(value.length());
        dataOutputStream.writeBytes(value);
    }

    /**
     * Reads a string array from the DataInputStream passed in as follows:
     * 1. Reads the array length as an integer n.
     * 2. Allocates a string array of size n.
     * 3. Iterates n times, reading a string each time into the string array.
     * @param dataInputStream The DataInputStream containing the bytes we are reading.
     * @throws IOException If fails to read from DataInputStream
     */
    public static String[] readStringArray(DataInputStream dataInputStream) throws IOException {
        int count = readInt(dataInputStream);
        String[] array = new String[count];
        for (int i = 0; i < count; i++) {
            array[i] = readString(dataInputStream);
        }
        return array;
    }

    /**
     * Writes a string array to the DataOutputString passed in as follows:
     * 1. Writes the string array length (n) as an integer.
     * 2. Iterates n times, writing a string from the array to the stream each time.
     * @param dataOutputStream DataOutputStream containing the byte array we are writing to
     * @param values The String values we are writing to the byte array
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeStringArray(DataOutputStream dataOutputStream, String[] values) throws IOException {
        writeInt(dataOutputStream, values.length);
        for (String value: values) {
            writeString(dataOutputStream, value);
        }
    }

    /**
     * Reads a string List from the DataInputStream passed in as follows:
     * 1. Reads the array length as an integer n.
     * 2. Allocates a string array of size n.
     * 3. Iterates n times, reading a string each time into the string List.
     * @param dataInputStream The DataInputStream containing the bytes we are reading.
     * @throws IOException If fails to read from DataInputStream
     */
    public static List<String> readStringList(DataInputStream dataInputStream) throws IOException {
        int count = readInt(dataInputStream);
        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(readString(dataInputStream));
        }
        return list;
    }

    /**
     * Writes a string List to the DataOutputString passed in as follows:
     * 1. Writes the string List length (n) as an integer.
     * 2. Iterates n times, writing a string from the List to the stream each time.
     * @param dataOutputStream DataOutputStream containing the byte array we are writing to
     * @param values The String values we are writing to the byte array
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeStringList(DataOutputStream dataOutputStream, List<String> values) throws IOException {
        writeInt(dataOutputStream, values.size());
        for (String value: values) {
            writeString(dataOutputStream, value);
        }
    }

    /**
     * Writes a Set of Strings to the output stream.
     * @param dataOutputStream The DataOutputStream we are writing to.
     * @param values The Set<String> we are writing.
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeStringSet(DataOutputStream dataOutputStream, Set<String> values) throws IOException {
        writeInt(dataOutputStream, values.size());
        for (String value: values) {
            writeString(dataOutputStream, value);
        }
    }

    /**
     * Reads a Set of Strings from the input stream.
     * @param dataInputStream The DataInputStream we are reading from.
     * @return The Set of Strings we read
     * @throws IOException If fails to read from the DataInputStream
     */
    public static Set<String> readStringSet(DataInputStream dataInputStream) throws IOException {
        int count = readInt(dataInputStream);
        Set<String> values = new HashSet<>();
        for (int i = 0; i < count; i++) {
            values.add(readString(dataInputStream));
        }
        return values;
    }


    /**
     * Reads a ChunkMetadata object from the DataInputStream as follows:
     * 1. Reads the version number as an int
     * 2. Reads the sequence number as an int
     * 3. Reads the timestamp as a long, converts to Timestamp object
     * 4. Reads the chunk size in bytes as an int
     * 5. Reads the string absolute filepath
     * @param dataInputStream DataInputStream containing ChunkMetadata we are reading
     * @return A ChunkMetadata instance created from the above fields
     * @throws IOException If fails to read from DataInputStream
     */
    public static ChunkMetadata readChunkMetadata(DataInputStream dataInputStream) throws IOException {
        int version = dataInputStream.readInt();
        int sequence = dataInputStream.readInt();
        long tsMillis = dataInputStream.readLong(); // read timestamp as long milliseconds since January 1, 1970, GMT
        Timestamp timestamp = new Timestamp(tsMillis);
        int sizeBytes = dataInputStream.readInt();
        String absoluteFilePath = readString(dataInputStream);
        return new ChunkMetadata(absoluteFilePath, version, sequence, timestamp, sizeBytes);
    }

    /**
     * Writes a ChunkMetadata object to the DataOutputStream as follows:
     * 1. Writes the chunk's version as an int
     * 2. Writes the chunk's sequence number as an int
     * 3. Writes the chunk's timestamp as a long
     * 4. Writes the chunk's size (in bytes) as an int
     * 5. Writes the file's absolute path as a string
     * @param dataOutputStream DataOutputStream we are writing the ChunkMetadata object to
     * @param metadata ChunkMetadata instance
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeChunkMetadata(DataOutputStream dataOutputStream, ChunkMetadata metadata) throws IOException {
        dataOutputStream.writeInt(metadata.getVersion());
        dataOutputStream.writeInt(metadata.getSequence());
        long tsMillis = metadata.getTimestamp().getTime(); // get timestamp as milliseconds since January 1, 1970, GMT
        dataOutputStream.writeLong(tsMillis);
        dataOutputStream.writeInt(metadata.getSizeBytes());
        writeString(dataOutputStream, metadata.getAbsoluteFilePath());
    }

    /**
     * Reads a List of ChunkMetadata objects from the DataInputStream as follows:
     * 1. Reads the List length as an integer n.
     * 2. Iterates n times, reading a ChunkMetadata instance each time into the List.
     * @param dataInputStream DataInputStream containing ChunkMetadata List we are reading
     * @return A List of ChunkMetadata instances created from the above fields
     * @throws IOException If fails to read from DataInputStream
     */
    public static List<ChunkMetadata> readChunkMetadataList(DataInputStream dataInputStream) throws IOException {
        int count = dataInputStream.readInt();
        List<ChunkMetadata> chunksMetadata = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            chunksMetadata.add(readChunkMetadata(dataInputStream));
        }
        return chunksMetadata;
    }

    /**
     * Writes a List of ChunkMetadata objects to the DataOutputStream as follows:
     * 1. Writes the number of ChunkMetadata objects in the List (n)
     * 2. Iterates n times, writing a ChunkMetadata object from the List to the stream each time.
     * @param dataOutputStream DataOutputStream we are writing the ChunkMetadata objects to
     * @param metadata List of ChunkMetadata instances
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeChunkMetadataList(DataOutputStream dataOutputStream, List<ChunkMetadata> metadata) throws IOException {
        dataOutputStream.writeInt(metadata.size());
        for (ChunkMetadata chunkMetadata: metadata) {
            writeChunkMetadata(dataOutputStream, chunkMetadata);
        }
    }

    /**
     * Writes a Chunk object to the DataOutputStream as follows:
     * 1. Writes the ChunkMetadata object
     * 2. Writes the ChunkIntegrity's slice checksums, a List of Strings
     * 3. Writes the raw chunk data, a byte array
     * @param dataOutputStream DataOutputStream we are writing the Chunk object to
     * @param chunk A Chunk object
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeChunk(DataOutputStream dataOutputStream, Chunk chunk) throws IOException {
        writeChunkMetadata(dataOutputStream, chunk.metadata);
        writeStringList(dataOutputStream, chunk.integrity.getSliceChecksums());
        dataOutputStream.write(chunk.data, 0, chunk.data.length);
    }

    /**
     * Reads a Chunk object from the DataInputStream as follows:
     * 1. Reads the ChunkMetadata object
     * 2. Reads the ChunkIntegrity's slice checksums, a List of Strings
     * 3. Reads the raw chunk data, a byte array
     * 4. Constructs and returns a Chunk from the above information
     * @param dataInputStream DataInputStream of the file we are reading from
     * @return Chunk Object we read from disk
     * @throws IOException If unable to read
     */
    public static Chunk readChunk(DataInputStream dataInputStream) throws IOException {
        ChunkMetadata metadata = Message.readChunkMetadata(dataInputStream);
        List<String> sliceChecksums = Message.readStringList(dataInputStream);
        ChunkIntegrity integrity = new ChunkIntegrity(sliceChecksums);
        byte[] chunkData = dataInputStream.readNBytes(metadata.getSizeBytes());
        return new Chunk(metadata, integrity, chunkData);
    }

    /**
     * Writes a FileMetadata object to the output stream.
     * @param dataOutputStream DataOutputStream we are writing to.
     * @param metadata FileMetadata object we want to write.
     * @throws IOException If fails to write to the DataOutputStream
     */
    public static void writeFileMetadata(DataOutputStream dataOutputStream, FileMetadata metadata) throws IOException {
        writeString(dataOutputStream, metadata.getAbsolutePath());
        Vector<Set<String>> chunkServerHostnames = metadata.getChunkServerHostnames();
        dataOutputStream.writeInt(chunkServerHostnames.size());
        for (Set<String> chunkHosts: chunkServerHostnames) {
            writeStringSet(dataOutputStream, chunkHosts);
        }
    }

    /**
     * Reads a FileMetadata object from the input stream.
     * @param dataInputStream DataInputStream we are reading from.
     * @return FileMetadata object we read from the input stream.
     * @throws IOException If fails to read from the DataInputStream.
     */
    public static FileMetadata readFileMetadata(DataInputStream dataInputStream) throws IOException {
        String absoluteFilePath = readString(dataInputStream);
        Vector<Set<String>> chunkServerHostnames = new Vector<>();
        int count = readInt(dataInputStream);
        for (int i = 0; i < count; i++) {
            chunkServerHostnames.add(readStringSet(dataInputStream));
        }
        return new FileMetadata(absoluteFilePath, chunkServerHostnames);
    }

    /**
     * Writes a List of FileMetadata objects to the output stream.
     * @param dataOutputStream DataOutputStream we are writing to.
     * @param metadataList List<FileMetadata> we are writing.
     * @throws IOException If fails to write to DataOutputStream
     */
    public static void writeFileMetadataList(DataOutputStream dataOutputStream, List<FileMetadata> metadataList)
            throws IOException {
        writeInt(dataOutputStream, metadataList.size());
        for (FileMetadata fileMetadata: metadataList) {
            writeFileMetadata(dataOutputStream, fileMetadata);
        }
    }

    /**
     * Reads a List of FileMetadata objects from the input stream.
     * @param dataInputStream DataInputStream we are reading from.
     * @return List<FileMetadata> we read
     * @throws IOException If fails to read from the DataInputStream.
     */
    public static List<FileMetadata> readFileMetadataList(DataInputStream dataInputStream) throws IOException {
        int count = readInt(dataInputStream);
        List<FileMetadata> metadataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            metadataList.add(readFileMetadata(dataInputStream));
        }
        return metadataList;
    }


    /**
     * Converts an integer to a MessageType enum
     * @param type integer type
     * @return MessageType enum
     */
    public static MessageType typeFromInteger(int type) {
        switch (type) {
            case 0: return MessageType.HEARTBEAT_MINOR;
            case 1: return MessageType.HEARTBEAT_MAJOR;
            case 2: return MessageType.CHUNK_STORE_REQUEST;
            case 3: return MessageType.CHUNK_STORE_RESPONSE;
            case 4: return MessageType.CLIENT_WRITE_REQUEST;
            case 5: return MessageType.CLIENT_WRITE_RESPONSE;
            case 6: return MessageType.CLIENT_READ_REQUEST;
            case 7: return MessageType.CLIENT_READ_RESPONSE;
            case 8: return MessageType.CHUNK_READ_REQUEST;
            case 9: return MessageType.CHUNK_READ_RESPONSE;
            case 10: return MessageType.CHUNK_REPLACEMENT_REQUEST;
            case 11: return MessageType.CHUNK_REPLACEMENT_RESPONSE;
            case 12: return MessageType.CHUNK_REPLICATION_INFO;
            case 13: return MessageType.CHUNK_CORRECTION_NOTIFICATION;
            case 14: return MessageType.CHUNK_REPLICATE_COMMAND;
            case 15: return MessageType.SYSTEM_REPORT_REQUEST;
            case 16: return MessageType.SYSTEM_REPORT_RESPONSE;
            default: return null;
        }
    }

    /**
     * Converts a MessageType enum to an integer
     * @param type MessageType enum
     * @return integer type
     */
    public static Integer integerFromType(MessageType type) {
        if (type == null) {
            throw new NullPointerException("MessageType cannot be null!");
        }
        switch (type) {
            case HEARTBEAT_MINOR: return 0;
            case HEARTBEAT_MAJOR: return 1;
            case CHUNK_STORE_REQUEST: return 2;
            case CHUNK_STORE_RESPONSE: return 3;
            case CLIENT_WRITE_REQUEST: return 4;
            case CLIENT_WRITE_RESPONSE: return 5;
            case CLIENT_READ_REQUEST: return 6;
            case CLIENT_READ_RESPONSE: return 7;
            case CHUNK_READ_REQUEST: return 8;
            case CHUNK_READ_RESPONSE: return 9;
            case CHUNK_REPLACEMENT_REQUEST: return 10;
            case CHUNK_REPLACEMENT_RESPONSE: return 11;
            case CHUNK_REPLICATION_INFO: return 12;
            case CHUNK_CORRECTION_NOTIFICATION: return 13;
            case CHUNK_REPLICATE_COMMAND: return 14;
            case SYSTEM_REPORT_REQUEST: return 15;
            case SYSTEM_REPORT_RESPONSE: return 16;
            default: return -1;
        }
    }
}
