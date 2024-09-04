package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void sendMessage() {
        try {
            bufferedWriter.write(username); // this.clientUsername = bufferedReader.readLine(); in ClientHandler constructor is waiting for that username write
            bufferedWriter.newLine();
            bufferedWriter.flush();

            var scanner = new Scanner(System.in); // get input from the console
            while (socket.isConnected()) { // while the client is connected we want to see what they write
                var messageToSend = scanner.nextLine(); // when user presses enter to send the message, it will be captured here
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Concurrency (Single Core) vs Parallel Execution (Multi-Core)
     * Concurrency: running two or more tasks in overlapping time phases - at any time there is only one task in execution.
     * Parallel: process is broken down into sub-tasks and multiple CPUs execute each sub-task at the same time. At any time all processes are executed.
     * Concurrency is about managing multiple tasks by interleaving them on a single CPU.
     * Parallelism is about executing multiple tasks at the same time using multiple CPUs.
     *
     * listenForMessage() is waiting for a message to be broadcast from ClientHandler#broadcastMessage.
     * Each client will have a separate thread that is waiting for the message, when they get it, they will print it out to each client's console.
     * */
    private void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        messageFromGroupChat = bufferedReader.readLine();
                        System.out.println(messageFromGroupChat);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();

    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
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

    public static void main(String[] args) throws IOException {
        var scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat: ");
        var username = scanner.nextLine();

        // make a connection to 1234 port
        var socket = new Socket("localhost", 1234);
        var client = new Client(socket, username);
        client.listenForMessage(); // blocking method
        client.sendMessage(); // blocking message but separate threads so will be able to run in the same time as the line above
    }

}
