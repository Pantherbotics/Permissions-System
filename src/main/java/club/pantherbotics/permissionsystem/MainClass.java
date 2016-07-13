package club.pantherbotics.permissionsystem;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import club.pantherbotics.permissionsystem.docker.DockerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by root on 7/13/16.
 */
public class MainClass {

    private static final int PORT = 1013;
    private final Logger logger = LogManager.getLogger("MainClass");
    private ServerSocket serverSocket;
    private Thread commandThread;
    private List<ClientThread> connectedClients;
    private boolean running;

    private MainClass() {
        start();
        run();
    }

    private void start() {
        logger.info("Starting Server");
        running = true;
        connectedClients = new ArrayList<>();
    }

    private void run() {
        try {
            commandThread = new Thread(this::commandListener, "Command Listener");
            commandThread.start();
            serverSocket = new ServerSocket(PORT);
            while (running) {
                final ClientThread thread = new ClientThread(serverSocket.accept());
                connectedClients.add(thread);
                thread.start();
            }
        } catch (SocketException ex) {
            logger.warn(ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void stop() {
        logger.info("Stopping Server");
        running = false;
        for (ClientThread connectedClient : connectedClients) {
            synchronized (connectedClient) {
                connectedClient.cancel();
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            serverSocket = null;
        }
        commandThread.interrupt();
        logger.debug("Command Thread Alive: {}", commandThread.isAlive());
        connectedClients.forEach(clientThread -> logger.debug("{} Thread Alive: {}", clientThread, clientThread.isAlive()));
        logger.info("Server Stopped");

    }

    private void commandListener() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                final String s = scanner.nextLine();
                if (s.equalsIgnoreCase("stop")) {
                    stop();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        DockerManager.i();
        new MainClass();
    }
}

/*
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"docker", "ps"};
        Process proc = rt.exec(commands);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

 */