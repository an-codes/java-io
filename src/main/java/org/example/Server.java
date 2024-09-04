package org.example;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Server is listening to the clients who wish to connect and when they do, creates a new thread to handle them.
 */
public class Server {

    /**
     * Responsible for incoming connections/clients and creating a socket to communicate with them
     */
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * startServer() keeps server running, we want the server to run until the serverSocket is closed
     */
    public void startServer() {

        try {

            while (!serverSocket.isClosed()) {
                /** while it is open we are waiting for the client to connect.
                 * accept is a blocking method, it halts until the client is connected, when the client connects it returns socket that is used to communicate with the client
                 */
                var socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                var clientHandler = new ClientHandler(socket);

                var thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        var serverSocket = new ServerSocket(1234);
        var server = new Server(serverSocket);
        server.startServer();
    }
}
