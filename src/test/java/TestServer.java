import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Socket;

/**
 * Created by root on 7/13/16.
 */
public class TestServer {

    @Test
    public void testServerConnection() throws Exception {
        Socket server = new Socket("localhost", 1013);
        server.getOutputStream().write("REQUEST USER BY username 'd4rkfly3r'".getBytes());
        server.getOutputStream().write("\n".getBytes());
        server.getOutputStream().flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        String line;
        while (server.isConnected() && !server.isClosed()) {
            try {
                line = in.readLine();
                if (line != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
