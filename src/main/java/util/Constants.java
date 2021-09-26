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

    // Time constants
    public static final short MS = 1;
    public static final short SEC = 1000 * MS;
    public static final int   MIN = 60 * SEC;

    // Service-specific constants
    public static final int CONTROLLER_PORT = 9001;
    public static final int CHUNK_SERVER_PORT = 9000;
    public static final int CHUNK_SIZE = 64 * KB;
    public static final int SLICE_SIZE = 8 * KB;
    public static final int CHUNK_REPLICATION = 3;
    public static final int HEARTBEAT_MINOR_INTERVAL = 30 * SEC;
    public static final int HEARTBEAT_MAJOR_INTERVAL = 5 * MIN;

    // Period of time that, after not receiving a heartbeat at the expected interval, we consider the Chunk Server dead
    public static final int HEARTBEAT_GRACE_PERIOD = 10 * SEC;

    // CLI help messages
    public static String CLI_CLIENT_HELP = "Run with command line inputs to change host and port:\n -h hostname \n -p port number";
    public static String CLI_SERVER_HELP = "Run with command line inputs to change port: \n -p port number";
}