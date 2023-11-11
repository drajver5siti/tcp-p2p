package org.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class TCPServer extends Thread {
    private final ServerSocket socket;
    private final Map<InetAddress, TCPPeer> peers;

    public TCPServer(int port, Map<InetAddress, TCPPeer> peers) throws RuntimeException {
        try {
            this.socket = new ServerSocket(port);
            this.peers = peers;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = this.socket.accept();
                var incomingAddress = socket.getInetAddress();

                if (this.peers.containsKey(incomingAddress)) {
                    continue;
                }

                var peer = new TCPPeer(socket);
                peer.start();
                this.peers.put(incomingAddress, peer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
