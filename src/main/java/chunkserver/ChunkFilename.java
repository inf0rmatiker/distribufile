package chunkserver;

/**
 * Handles the encoding of chunk metadata into the filename sent by the client,
 * and decoding of the filename from how we've stored it.
 *
 * Example: If the absolute path of a file, according to the client, is /path/to/my/file.data,
 * and we are storing chunk 3 of that file, then we break the name into the following parts:
 * chunkDir() - returns "/tmp"
 * base - "/path/to/my/"
 * filename - "file.data"
 * chunkSuffix - "_chunk3"
 *
 * These parts, when assembled as the absolute path of the chunk file, result in:
 * "/tmp/path/to/my/file.data_chunk3"
 */
public class ChunkFilename {

    public String chunkDir;
    public String base;
    public String filename;
    public String chunkSuffix;

    /**
     * Builds all the parts of a chunk's filename from the client's absolute path and provided chunk metadata.
     * @param absolutePath The client's provided absolutePath
     * @param chunkDir The directory we are storing our chunk files in
     * @param chunkSequence The sequence number of the chunk
     */
    public ChunkFilename(String absolutePath, String chunkDir, Integer chunkSequence) {
        this.chunkDir = chunkDir;

        // Split absolute path by '/', then take the final element as the filename.
        String[] fields = absolutePath.split("/");
        this.filename = fields[fields.length - 1];

        // Remove the filename from the absolute path for the base
        this.base = absolutePath.substring(0, absolutePath.length() - this.filename.length());

        // Build chunk suffix
        this.chunkSuffix = String.format("_chunk%d", chunkSequence);
    }

    /**
     * Builds all the parts of a chunk's filename from the encoded absolute path of the chunk file.
     * @param chunkFilename The encoded absolute path of the chunk file
     * @param chunkDir The directory we are storing our chunk files in
     */
    public ChunkFilename(String chunkFilename, String chunkDir) {
        this.chunkDir = chunkDir;

        // Get chunk suffix
        int chunkSuffixIndex = chunkFilename.lastIndexOf("_chunk");
        this.chunkSuffix = chunkFilename.substring(chunkSuffixIndex);

        // Get the client absolute path of the file by chopping off chunk directory prefix and chunk suffix
        String absolutePath = chunkFilename.substring(chunkDir.length(), chunkSuffixIndex);

        // Split absolute path by '/', then take the final element as the filename.
        String[] fields = absolutePath.split("/");
        this.filename = fields[fields.length - 1];

        // Remove the filename from the absolute path for the base
        this.base = absolutePath.substring(0, absolutePath.length() - this.filename.length());
    }

    public String getChunkDir() {
        return chunkDir;
    }

    public String getBase() {
        return base;
    }

    public String getFilename() {
        return filename;
    }

    public String getChunkSuffix() {
        return chunkSuffix;
    }

    /**
     * @return The String absolute path of the file as it appears to the client
     */
    public String getClientAbsolutePath() {
        return String.format("%s%s", base, filename);
    }

    /**
     * @return The String absolute path of the chunk file
     */
    public String getChunkFilename() { return String.format("%s%s%s%s", chunkDir, base, filename, chunkSuffix); }

    /**
     * @return The String base of the chunk file
     */
    public String getChunkBase() { return this.chunkDir + this.base; }

    /**
     * @return The Integer sequence of the chunk
     */
    public Integer getChunkSequence() {
        return Integer.parseInt(getChunkSuffix().replaceFirst("_chunk", ""));
    }

    @Override
    public String toString() {
        return getChunkFilename();
    }

}
