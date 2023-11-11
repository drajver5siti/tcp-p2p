package org.example;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;

public class TCPKiller extends Thread{

    private static final long THRESHOLD_SECONDS = 30;
    private static final long INTERVAL_MILLIS = 1000;

    private final Map<InetAddress, TCPPeer> peers;

    public TCPKiller(Map<InetAddress, TCPPeer> peers) {
        this.peers = peers;
    }

    @Override
    public void run() {
        while (true) {
            LocalDateTime threshold = LocalDateTime.now().minusSeconds(TCPKiller.THRESHOLD_SECONDS);
            this.peers.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().lastMessage.isBefore(threshold))
                    .forEach(entry -> {
                        System.out.printf(
                                "SYSTEM: Auto closing connection with %s because the threshold timer of %d seconds has exceeded.\n",
                                entry.getKey().getHostAddress(),
                                TCPKiller.THRESHOLD_SECONDS
                        );
                        peers.remove(entry.getKey());
                        entry.getValue().killConnection();
                    });
            try {
                Thread.sleep(TCPKiller.INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
