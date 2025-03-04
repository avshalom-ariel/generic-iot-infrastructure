package plugAndPlay;

import rps.RPS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;

public class DirMonitor {
    private WatchService watchService = null;;
    private volatile boolean running;
    private RPS rps = null;

    public DirMonitor(String dirToWatch, RPS rps) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path pathToWatch = Paths.get(dirToWatch);
            if (!Files.exists(pathToWatch)) {
                throw new FileNotFoundException("File not found!");
            }
            if (!Files.isDirectory(pathToWatch)) {
                throw new IllegalArgumentException("File is not a directory!");
            }

            pathToWatch.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.rps = rps;
    }

    public void watch() throws InterruptedException, IOException {
        running = true;

        while (running) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                String message = (new Date()) + " " + watchEvent.kind() + " " + watchEvent.context() + "\n";
                if (watchEvent.kind() != StandardWatchEventKinds.ENTRY_DELETE) {
                    System.out.println(message);
                    rps.load(String.valueOf(watchEvent.context()));
                }
            }

            key.reset();
        }
    }

    public void stop() {
        running = false;
    }
}