package server;

import common.ConcentrationException;

import java.io.IOException;
import java.net.ServerSocket;
/**
 * Server Class
 *
 *
 */
public class ConcentrationServer {
    private int clientId = 0;

    /**
     * Run method
     * Creates socket and initialize server-client thread
     * @param portNumber of server-client thread
     * @param dimension of gameboard
     */
    private void run(int portNumber, int dimension) {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println(String.format("Concentration server starting on port %d, DIM=%d", portNumber, dimension));
            while (true) {
                try {
                    new ConcentrationClientServerThread(serverSocket.accept(), dimension, ++clientId).start();
                } catch (ConcentrationException e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
    /**
     * Main class that assigns arguments and calls runs method.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ConcentrationServer <port number> <board dimension>");
            return;
        }
        int portNumber = Integer.parseInt(args[0]);
        int dimension = Integer.parseInt(args[1]);

        ConcentrationServer server = new ConcentrationServer();
        server.run(portNumber, dimension);
    }
}
