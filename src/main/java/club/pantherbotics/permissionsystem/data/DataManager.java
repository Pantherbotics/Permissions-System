package club.pantherbotics.permissionsystem.data;

import club.pantherbotics.permissionsystem.docker.DockerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Created by root on 7/13/16.
 */
public class DataManager {

    private final Path ROOT_FOLDER_PATH = FileSystems.getDefault().getPath("/", "pantherbotics", "projects", "permissions", "pdd");

    private static final Logger logger = LogManager.getLogger("DataManager");
    private static DataManager ourInstance = new DataManager();
    private FileLock lock;

    public static DataManager i() {
        return ourInstance;
    }

    private DataManager() {
        try {
            FileChannel channel = new RandomAccessFile(ROOT_FOLDER_PATH.toFile(), "rw").getChannel();

            // Try acquiring the lock without block ing. This method returns
            // null or throws an exception if the file is already locked.
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException e) {
                logger.fatal(e.getMessage());
                System.exit(1);
                // File is already locked in this thread or virtual machine
            }
        } catch (Exception e) {
            //CAN'T FIND FILE
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public String getOrCreateUser(String username) {
        logger.debug("Getting or creating user: '{}'", username);
        final Path userFolder = ROOT_FOLDER_PATH.resolve("default").resolve(username);
        System.out.println(userFolder);
        if (Files.notExists(userFolder)) {
            try {
                Files.createDirectories(userFolder);
                DockerManager.i().startBaseContainer(username);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return Files.walk(userFolder).filter(path -> {
                try {
                    return !Files.isSameFile(userFolder, path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }).map(path -> path.toFile().getName()).collect(Collectors.joining("|"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "FAIL";
    }
}
