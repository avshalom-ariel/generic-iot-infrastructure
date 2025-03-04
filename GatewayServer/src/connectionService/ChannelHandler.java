package connectionService;

import java.io.IOException;

public interface ChannelHandler {

    void handle() throws IOException;
}
