package messaging;

public class ChunkStoreRequest extends Message {


    @Override
    public MessageType getType() {
        return MessageType.CHUNK_STORE_REQUEST;
    }
}
