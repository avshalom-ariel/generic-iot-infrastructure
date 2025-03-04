package connectionService;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface RespondableChannel {
    public void respond(ByteBuffer bytes) throws IOException;
}
