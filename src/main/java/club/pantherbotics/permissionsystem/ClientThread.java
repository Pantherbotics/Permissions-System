package club.pantherbotics.permissionsystem;

import club.pantherbotics.permissionsystem.data.DataManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


/**
 * Created by root on 7/13/16.
 */
public class ClientThread extends Thread {
    private static final Logger logger = LogManager.getLogger("ClientThread");

    private final Socket client;

    public ClientThread(Socket client) {
        this.client = client;
        logger.debug("Client Connected: {}", client);
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line;
            while (client.isConnected() && !client.isClosed()) {
                try {
                    line = in.readLine();
                    if (line != null) {
                        logger.debug("Executing statement: {}", line);
                        String userString = line.split(" ")[4];
                        userString = userString.substring(1, userString.length() - 1);
                        final String userData = DataManager.i().getOrCreateUser(userString);
                        client.getOutputStream().write(("USER LOOKUP RESULTS FOR '" + userString + "' ARE '" + userData + "'").getBytes());
                        client.getOutputStream().write("\n".getBytes());
                        client.getOutputStream().flush();
                        client.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
