package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Communicating with the client, implement Runnable (implemented on a class which each thread wil be a different instance. each new client = each new thread)
 * Threads share memory space. Launching executable is running a thread within a process.
 * Instances of classes that implement a Runnable can be passed as an argument to new Thread() and be run on a thread. Instances will be executed as separate threads.
 *
 * Java has 2 types of streams - byte stream(...Stream) and character stream(...Writer/Reader). Here we want character stream because we are sending messages.
 * buffer(characterStream(byteStream))
 * buffer makes communication more efficient due to the way data is read or written in chunks rather than one byte or character at a time
 */
public class ClientHandler implements Runnable {

    /**
     * Keeps track of all our clients.
     * When a client sends a message we can loop through the list of our clients and send that to each one.
     * Static - we want it to belong to a class.
     */
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    /**
     * Passed from the server class. Used to establish connection between client and server.
     * Each Socket represents a connection between server/client handler and the client.
     * Each has an input stream to read data that wherever you are connected to sent you and output stream to send data to wherever you are connected to.
     */
    private  Socket socket;
    /**
     * Used to read data, messages sent by the clients.
     */
    private BufferedReader bufferedReader;
    /**
     * Used to send data, messages to clients, messages that have been sent from other clients.
     */
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {

            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            /**
             * The first line from the screen where the client is asked to input a username, when the client presses enter,
             * they send a "string/n", and we want to read the line until the new line character (/n)
             */
            this.clientUsername = bufferedReader.readLine();
            /**
             * "this" represents the ClientHandler object we are creating.
             */
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has entered the chat.");

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Everything that is run in this method is run on a separate thread.
     * We need to listen to the messages on the separate threads because it is a blocking operation.
     * Blocking operation means that the program is stopped until the operation is completed.
     */
    @Override
    public void run() {
        /** Holds a message received from the client */
        String messageFromClient;

        /** When we are connected to the client, let's listen to the messages. */
        while(socket.isConnected()) {

            try {
                messageFromClient = bufferedReader.readLine(); // this is a blocking operation, hence a separate thread.
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }

        }
    }

    private void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {

            try {
                if (clientHandler != null && !clientHandler.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    /**
                     * equivalent of pressing enter. messageFromClient is waiting for a new line because "Reads a line of text. A line is considered to be terminated by any one of a line feed ('\n')"
                     */
                    clientHandler.bufferedWriter.newLine();
                    /**
                     * A buffer will not be sent down the stream unless it is full. Messages will not fill the whole buffer, so we manually flush.
                     * Flush - Transfers all data to the reader endpoint before data flow resumes.
                     * */
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            // no need to close Output/InputReader separately
            if (socket != null) {
                socket.close();
            }

            if (bufferedReader != null) {
                bufferedReader.close();
            }

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void removeClientHandler() {
        clientHandlers.remove(this); // this - current clientHandler
        broadcastMessage("SERVER: " + clientUsername + " has left the chat.");
    }
}
