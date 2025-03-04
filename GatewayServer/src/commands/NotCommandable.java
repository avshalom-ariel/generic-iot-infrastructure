package commands;

import java.util.concurrent.*;

public class NotCommandable implements Executor {
    @Override
    public void execute(Runnable command) {
        command.run();
    }
    public static void main(String[] args) {}

}
