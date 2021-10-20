package Satvik.main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BeatBoxServer {
    List<ObjectOutputStream> clientOutputStreams;

    public static void main(String[] args) {
        BeatBoxServer server = new BeatBoxServer();
        server.start();
    }

    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            int clientNum = 1;
            clientOutputStreams = new ArrayList<>();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream writer = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(writer);

                // start a new thread which will allow the server to listen for
                // messages from each client without blocking other operations
                Thread t = new Thread(new ClientHandler(clientSocket, "Client "+clientNum));
                t.start();
                System.out.println("Got a connection!");
                clientNum++;
            }
        } catch(IOException ex){ ex.printStackTrace(); }
    }

    private class ClientHandler implements Runnable {

        private Socket clientSocket;
        private ObjectInputStream clientReaderStream;
        private String clientName;

        public ClientHandler(Socket socket, String name){
            try {
                clientSocket = socket;
                clientReaderStream = new ObjectInputStream(socket.getInputStream());
                this.clientName = name;
            } catch(IOException ex){
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Message message;
                while ((message = (Message) clientReaderStream.readObject()) != null) {
                    System.out.println("Got a message from " + clientName+":\n"+message.toString());

                    broadcast(message);
                }
            } catch (IOException | ClassNotFoundException ex){
                System.out.println("Error while reading message from "+clientName);
                ex.printStackTrace();
            }
        }
    }

    private void broadcast(Message message) {
        clientOutputStreams.forEach(outputStream -> {
            try {
                outputStream.writeObject(message);
            } catch (IOException ex) {
                System.out.println("Error while broadcasting the message");
                ex.printStackTrace();
            }
        });
    }
}


