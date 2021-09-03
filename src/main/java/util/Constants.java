package util;

/**
 * A cheap way of managing constants.
 * In the future, we may want to look into a config file or environment variables
 * which get loaded at runtime.
 */
public class Constants {

    // Human-readable storage constants
    public static final short BYTE = 1;
    public static final short KiB = 1024 * BYTE, KB = 1000 * BYTE;
    public static final int   MiB = 1024 * KiB, MB = 1000 * KB;
    public static final long  GiB = 1024 * MiB, GB = 1000 * MB;
    public static final long  TiB = 1024 * GiB, TB = 1000 * GB;

    // Service-specific constants
    public static final int CONTROLLER_PORT = 9001;
    public static final int CHUNK_SERVER_PORT = 9000;
    public static final int CHUNK_SIZE = 64 * KB;
}