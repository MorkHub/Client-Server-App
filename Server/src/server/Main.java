package server;

import server.message.MessageEvent;
import server.message.MessageHandler;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class Main {
    MessageHandler messageHandler = new MessageHandler();

    public void registerHandlers() {
        messageHandler.register("WHOAMI", (String args) -> {

        });
    }

    public static void main(String[] args) {
        new Main(Integer.parseInt(args[0]));
    }

    public Main(int portNumber) {
        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {

            String message;
            while ((message = in.readLine()) != null) {
                // FIXME: use actual things here
                messageHandler.handleMessage(message, null, null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
