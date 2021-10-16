# distribufile

# Table of Contents
1. [System Description](#System Description)
2. [Usage](#Usage)

_Authors_: [Caleb Carlson](https://github.com/inf0rmatiker), [Matthew Fernst](https://github.com/matthewfernst)

A distributed, replicated, and fault-tolerant file system written in Java. This project was developed for [CS555: Distributed Systems](https://www.cs.colostate.edu/~cs555/index.html), [Assignment 1](docs/CS555-Fall2021-HW1.pdf).

## System Description

The whole system provides a scalable way for users to add new files, update/edit existing files, and retrieve files in a resilient and efficient manner across many machines.
These machines run one of 3 logical components to the system: the Client, Chunk Servers, and Controller. 

### Chunks

Files are broken up into 64 KB "chunks" (this can be configured) and stored separately on Chunk Servers, with a replication level of *R* (default 3, but can be configured).
Due to unavoidable internal fragmentation, the last chunk of a file may be less than 64 KB. Within a chunk, SHA-1 hashes are calculated for each 8 KB slice, and stored with the chunk on disk.
No two copies of the same chunk are stored on the same Chunk Server; this is strictly enforced by the Controller.

### Client

The Client's responsibility is to interface with the distributed filesystem, similarly to how Hadoop's [HDFS FS Shell](https://hadoop.apache.org/docs/r1.2.1/hdfs_design.html) works.
The Client currently supports the following operations:

- **Read**: The client is able to retrieve a file by its absolute path on the chunk server. For example, if a file */path/to/my/file.data* had been previously stored
on the distributed filesystem, a read command can be issued to retrieve that file in its entirety. An output filename must be specified, which will be used when saving
the file to the Client's local storage. To read a file, the following steps are performed by the client:
  1. A `CLIENT_READ_REQUEST` message is sent to the Controller node, containing the name of the file it wishes to read.
  2. The Controller node responds with a `CLIENT_READ_RESPONSE`, containing an ordered list of chunk metadata for the file and which Chunk Servers hold the corresponding chunks.
  3. The Client creates a `BufferedFileWriter` to a new file, specified by the program args, then, 
  4. For each chunk in the metadata list, the client reaches out to the Chunk Server which contains that chunk with a `CHUNK_READ_REQUEST`
  5. Upon receiving the chunk from the Chunk Server in a `CHUNK_READ_RESPONSE`, the Client buffers the chunk write to the file it opened
  6. If the end of the chunk metadata list is reached, or a chunk version is received that is older than the versions that were previously received, the read process is completed

    *Note: The Client notifies the user of each chunk's integrity status as they are received.*


- **Write**: The client is able to both write a new file, or make updates to an existing file. The absolute path of the file to write must be used, so if the client wants to
write *file.data* in the current directory, then the absolute path `/path/to/my/file.data` must be used. Subsequent writes to the same file will increment the version number of the file,
and upon retrieval, the latest one will be returned. To write a file, the Client performs the following operations:
  1. A `BufferedFileReader` is opened on the file that resides on the Client's local filesystem.
  2. For each chunk read from the buffered reader, the Client makes a `CLIENT_WRITE_REQUEST` to the Controller node, asking it for a suitable Chunk Server to store the chunk at
  3. The Client receives a `CHUNK_WRITE_RESPONSE` from the Controller, containing the hostname/IP address of a suitable Chunk Server, then,
  4. The Client reaches out to the Chunk Server with a `CHUNK_STORE_REQUEST`, containing the chunk data to be stored
  5. The Chunk Server then responds with a `CHUNK_STORE_RESPONSE`, with the success/failure status of storage
  6. If the status was success, the Client repeats the process with the next buffered chunk of the file, and if failure, asks the Controller for another Chunk Server


- **System Status Report**: This is mainly for diagnostic information. The Client sends a `SYSTEM_REPORT_REQUEST` to the Controller node, which responds with a
`SYSTEM_REPORT_RESPONSE` containing information about all the tracked Chunk Servers, the chunks they hold, the files maintained in the filesystem, and metadata about each of the chunks for the files.

### Controller

The Controller's responsibility is to field read/write requests from the Client, and manage the Chunk Servers. Furthermore, the Controller tracks information in-memory
about all files maintained, metadata each of the chunks belonging to a file, as well the *R* replica Chunk Servers that store that chunk.

- **Client Write Requests**: When a Controller receives a `CLIENT_WRITE_REQUEST` from a Client, its job is to determine *R* suitable Chunk Servers to store that chunk at.
These Chunk Servers are selected based on current load (total number of chunks already maintained) and free space available at the chunk storage location. For efficiency,
the *R* Chunk Servers are selected using [Unordered Partial Sorting](https://en.wikipedia.org/wiki/Selection_algorithm#Unordered_partial_sorting), a k-selection algorithm with a
complexity of _O(kn)_, better than _O(nlog(n))_. No chunks are duplicated on a single Chunk Server, so we end up with uniform distribution of chunks.
  1. Upon determining the Chunk Servers a given chunk should be stored at, the Controller tracks the file if it has not seen it before, then,
  2. Responds to the client with a `CLIENT_WRITE_RESPONSE` containing the *R* Chunk Servers' identifying network information.
  3. Once the Chunks have all received and stored that chunk, the Controller is asynchronously updated with that metadata information via heartbeats.


- **Client Read Requests**: When a Controller receives a `CLIENT_READ_REQUEST` from a Client, it constructs a `CLIENT_READ_RESPONSE` with a list of Chunk Servers
storing each of the chunks in the requested file, and sends it back to the Client.


- **Heartbeats**: Heartbeats are the main method a Controller uses for tracking the health of a Chunk Server, the chunks stored there, and any corruptions which may have occurred.
Heartbeat messages are expected from each of the Chunk Servers every 30 seconds, with a grace period of 10 seconds. A `HeartbeatMonitor` task is implemented which monitors
if a heartbeat has not been received from a given Chunk Server within the last 30 seconds. There are two types of heartbeat messages, handled differently:
  - **Minor Heartbeats**: Sent once every 30 seconds, and contains information about the free space available on the Chunk Server, the total number of chunks maintained, any newly stored chunks, and any
  chunks which have had corruption detected. 
    1. If there are new chunks reported, these are added to both the Chunk Server metadata lists and the tracked file metadata lists.
    2. If there are corrupted chunks reported the Controller initiates a repair of the corrupted chunk by responding with a `CHUNK_REPLICATION_INFO` message,
      and removing that Chunk Server from the chunk hosts set until such time that it has been repaired and validated.
    3. The heartbeat timestamp is updated
  - **Major Heartbeats**: Sent once every 5 minutes, and contains information about the free space available on the Chunk Server, total number of chunks maintained, and metadata for 
  chunk maintained.
    1. For each chunk metadata, the Chunk Server metadata is updated to contain it if not already, similarly to the file metadata.
    2. The heartbeat timestamp is updated
  - **Expiring a Chunk Server**: If no heartbeat has been received for a given Chunk Server in the 30 seconds + grace period, the `HearbeatMonitor` initiates an expiration of the Chunk Server:
    1. That Chunk Server is assumed to be dead, so it is removed from all possible chunk host lists
    2. Everything that the expired Chunk Server maintained chunk-wise needs to be replicated elsewhere, so the Controller sends `CHUNK_REPLICATE_COMMAND` messages 
    to one of the other Chunk Servers maintaining a replica of the chunks the dead Chunk Server had to replicate the Chunk on another living Chunk Server which does not already have that chunk.
    3. In this fashion, the replication level for each chunk is restored back to *R*
    

- **System Reports**: Responds to Client `SYSTEM_REPORT_REQUEST` messages with a `SYSTEM_REPORT_RESPONSE` message, containing all the currently-tracked
metadata for files and Chunk Servers.


### Chunk Server

Responsible for storing chunks, initiating replication of chunks to other Chunk Servers, retrieving chunks for a Client, and maintaining/validating the integrity of the
stored chunks.

- **Storing Chunks**: Receives a chunk in a `CHUNK_STORE_REQUEST` from either a Client directly, or another Chunk Server. To store the chunk:
  1. Checks if that chunk already exists on disk. If so, treats it as an update by saving the new chunk with an incremented version number
  2. To save a chunk, integrity information is calculated for each 8 KB slice of the chunk, and stored as part of the file metadata
  3. The chunk is then written to disk, using the original filename provided by the Client, with `_chunk<sequence>` appended,
    where `<sequence>` is the sequence number of the chunk within the file.
  4. Achieves a replication of level *R* by removing itself from the list of Chunk Servers to forward the `CHUNK_STORE_REQUEST` to, and
  5. If the replication list is empty, responding with a `CHUNK_STORE_RESPONSE`, or
  6. If there are more recipient Chunk Servers to forward the message to, forward it to the next in the list, and return the response from upstream


- **Retrieving Chunks**: Receives a `CHUNK_READ_REQUEST`, with the filename and sequence number of the chunk to retrieve
  1. Chunk is read from disk, containing the metadata and integrity information of the file
  2. The read integrity information is validated against calculated integrity of the chunk data read
  3. If the integrity is found to be valid, a `CHUNK_READ_RESPONSE` is sent back to the Client with the chunk data
  4. If the integrity is found to be invalid, a `HEARTBEAT_MINOR` message is sent to the Controller with information about the corrupted chunk
  5. Upon receipt of a `CHUNK_REPLICATE_INFO`, containing another Chunk Server with a valid copy, the Chunk Server sends a `CHUNK_REPLICATE_REQUEST` to the replica Chunk Server
  6. Upon receipt of a `CHUNK_REPLICATE_RESPONSE` from that Chunk Server, the new chunk copy is validated and stored, and the Chunk Server sends a `CHUNK_CORRECTION_NOTIFICATION`
  to the Controller, notifying it that it has corrected its copy of the chunk and can be used once again as a host for that chunk
  7. The Chunk Server then proceeds with returning the chunk to the requesting Client


- **Chunk Integrity**: SHA-1 hashes are calculated for each 8 KB slice of the 64 KB chunk, and stored as part of the chunk's metadata on disk.
  - These hashes are used to check if the file data has been modified or altered, and if either mismatch, the file is treated as corrupted
  - If unable to read even the metadata, the file is obviously treated as corrupted as well

## Usage

### Configuration

You must first specify the Controller node hostname and Chunk Server hostnames by editing the `config/` files,
with 1 hostname per line in each of the respective files. Use a minimum of 3 Chunk Servers.

**Example** *config/controller*:
```
shark
```

**Example** *config/chunkservers*:
```
tuna
sole
swordfish
halibut
bass
```

### Building

Just clone this repository to one of your machines in the cluster, and build it with `./gradlew clean build`.

### Running

Once you have the configuration files established and the program built, use `distribufile.sh` to run the system (this requires key-less SSH):

```
USAGE
	distribufile.sh [OPTIONS]

OPTIONS

	--cleanup                  Remove all artifacts/chunks stored on all Chunk Servers in cluster

	--shutdown                 Shuts down all Chunk Servers first, then the Controller

	--chunkservers             Starts the Chunk Servers

	--controller               Starts the Controller

	--client-read <file> <out> Reads a file specified by <file> and outputs it as <out> file

	--client-write <file>      Writes a file specified by <file> to the distributed filesystem

	--status                   Retrieves a diagnostic report of the system from the Controller
```

**Example**: 
```bash
./distribufile.sh --controller
./distribufile.sh --chunkservers
./distribufile.sh --client-write /path/to/my/file.data
./distribufile.sh --status
./distribufile.sh --client-read /path/to/my/file.data retrieved_file.data
```