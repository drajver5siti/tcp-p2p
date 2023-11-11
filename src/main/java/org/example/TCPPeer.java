package org.example;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TCPPeer extends Thread {

    Socket socket;
    BufferedReader input;
    BufferedWriter output;

    LocalDateTime lastMessage = LocalDateTime.now();
    boolean exit = false;

    public TCPPeer(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendMessage(Message message) throws IOException {
        this.lastMessage = LocalDateTime.now();
        this.output.write(message.toJSON());
        this.output.newLine();
        this.output.flush();
    }

    public void killConnection() {
        this.exit = true;
        try {
            this.socket.close();
            this.input.close();
            this.output.close();
        } catch (IOException e) {}
    }

    @Override
    public void run() {
        String line;
        while (!this.exit) {
            try {
                line = this.input.readLine();
                if (line == null) {
                    break;
                }

                this.lastMessage = LocalDateTime.now();
                var message = Message.fromJSON(line);

                if (message.fileContents.isEmpty()) {
                    System.out.printf(
                            "Message from %s: %s\n",
                            message.sender,
                            message.messageText
                    );
                    continue;
                } else {
                    System.out.printf(
                            "File message from %s, file name is %s\nFile content:\n%s\n",
                            message.sender,
                            message.messageText,
                            message.fileContents
                                    .stream()
                                    .reduce((a, b) -> a + "\n" + b)
                                    .get()
                    );
                    continue;
                }
            } catch (IOException e) {
                this.killConnection();
            }
        }
    }
}
