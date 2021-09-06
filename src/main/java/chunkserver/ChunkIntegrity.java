package chunkserver;

import java.util.List;

public class ChunkIntegrity {

    // A list of SHA-1 checksums, one for each slice of the chunk
    public List<String> sliceChecksums;

    public List<String> getSliceChecksums() {
        return sliceChecksums;
    }


}
