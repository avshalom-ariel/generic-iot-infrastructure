package commands;

import connectionService.RespondableChannel;

import java.io.IOException;

public interface Command {
    void execute(RespondableChannel respondableChannel) throws IOException;
}
