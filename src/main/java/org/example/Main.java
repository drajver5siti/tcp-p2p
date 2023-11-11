package org.example;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Main {

    private static final int PORT = 9753;
    private static final Map<String, InetAddress> hostnameCache = new HashMap<>();
    private static final Map<InetAddress, TCPPeer> peers = new ConcurrentHashMap<>();

    // Matches connect:(ip|hostname)
    // connect:localhost
    // connect:127.0.0.1
    private static final Pattern CONNECT_PATTERN = Pattern.compile("^connect:(.+)$");

    private static InetAddress resolveHostname(String hostname) throws UnknownHostException {
        if (Main.hostnameCache.containsKey(hostname)) {
            return Main.hostnameCache.get(hostname);
        }

        return InetAddress.getByName(hostname);
    }

    private static TCPPeer connect(InetAddress host, int port) throws IOException {
        var peer = new TCPPeer(new Socket(host, port));
        peer.start();
        return peer;
    }

    private static Optional<TCPPeer> resolvePeer(String hostname) {
        try {
            var ip = Main.resolveHostname(hostname);
            var peer = peers.get(ip);

            if (peer == null) {
                return Optional.empty();
            }

            return Optional.of(peer);
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        (new TCPServer(Main.PORT, peers)).start();
        (new TCPKiller(peers)).start();

        var reader = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while (true) {
            try {
                line = reader.readLine();
            } catch (IOException e) {
                System.out.println("SYSTEM: Unable to read input from System.in, program will terminate");
                System.exit(-1);
                return;
            }

            if (line.equals("log")) {
                System.out.println(peers);
                continue;
            }

            var matcher = Main.CONNECT_PATTERN.matcher(line);
            if (matcher.matches()) {
                var hostname = matcher.group(1);
                InetAddress address;

                try {
                    address = Main.resolveHostname(hostname);
                } catch (UnknownHostException e) {
                    System.out.printf("SYSTEM: Unable to resolve hostname %s\n", hostname);
                    continue;
                }

                if (peers.containsKey(address)) {
                    continue;
                }

                try {
                    var peer = Main.connect(address, Main.PORT);
                    Main.hostnameCache.put(hostname, address);
                    peers.put(address, peer);
                    continue;
                } catch (IOException e) {
                    System.out.printf("SYSTEM: Unable to establish connection with %s\n", hostname);
                    continue;
                }
            }

            matcher = Pattern.compile("^(.+):.+$").matcher(line);
            if (!matcher.matches()) {
                System.out.println("SYSTEM: Invalid input !");
                continue;
            }

            var hostname = matcher.group(1);
            var peer = Main.resolvePeer(hostname);
            if (peer.isEmpty()) {
                System.out.printf("SYSTEM: Unable to resolve peer with address: %s, try to connect with connect:hostname\n", hostname);
                continue;
            }

            try {
                var message = MessageFactory.build(line);
                message.sender = peer.get().socket.getInetAddress().getHostAddress();
                peer.get().sendMessage(message);
            } catch (FileNotFoundException|InvalidInputException|UnknownHostException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println("SYSTEM: Unable to send message!");
            }
        }
    }
}