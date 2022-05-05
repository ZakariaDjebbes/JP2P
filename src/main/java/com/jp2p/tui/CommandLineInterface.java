package com.jp2p.tui;

import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class CommandLineInterface {
    public static void main(String[] args) {
        Startup(args);
    }

    private static void Startup(String[] args) {
        try {
            String peerName = args[0] + new Random().nextInt(15000);
            PeerRunner peer = new PeerRunner(peerName, new Random().nextInt(1026, 65535), 10);
            new Thread(peer).start();
            Thread.sleep(1000);

            System.out.println("""
                    List of commands (type help to see it again or clear to clear)
                    1 - name [hostname] [port number]
                    2 - knownPeers [hostname] [port number]
                    3 - itsMe [hostname] [port number]
                    4 - file [fileName.ext] [bounces]
                    5 - download [fileName.ext] [bounces]
                    6 - bye [hostname] [port number]
                    """);

            while (true) {
                System.out.printf("%s>", peer.getName());
                String userCommand = new Scanner(System.in).nextLine();
                invokeCommand(userCommand, peer);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Expecting the name of the peer as an argument which was not found.");
        }
    }

    public static void invokeCommand(String command, PeerRunner peer) throws IOException, ClassNotFoundException, InterruptedException {
        String[] args = command.split(" ");
        try {
            switch (args[0]) {
                case "help" -> System.out.println("""
                        List of commands (type help to see it again or clear to clear)
                        1 - name [hostname] [port number]
                        2 - knownPeers [hostname] [port number]
                        3 - itsMe [hostname] [port number]
                        4 - file [fileName.ext] [bounces]
                        5 - download [fileName.ext] [bounces]
                        6 - bye [hostname] [port number]
                        """);

                case "clear" -> {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }

                case "name" -> {
                    String hostname = args[1];
                    String port = args[2];

                    String res = peer.sendGetName(new Socket(hostname, Integer.parseInt(port)));
                    System.out.println(res);
                }

                case "knownPeers" -> {
                    String hostname = args[1];
                    String port = args[2];

                    String res = peer.sendGetKnownPeers(new Socket(hostname, Integer.parseInt(port)));
                    System.out.println(res);
                }

                case "itsMe" -> {
                    String hostname = args[1];
                    String port = args[2];

                    String res = peer.sendItsMe(new Socket(hostname, Integer.parseInt(port)));
                    System.out.println(res);
                }

                case "bye" -> {
                    String hostname = args[1];
                    String port = args[2];

                    String res = peer.sendBye(new Socket(hostname, Integer.parseInt(port)));
                    System.out.println(res);
                }

                case "file" -> {
                    String fileName = args[1];
                    String bounces = args[2];

                    Peer p = peer.sendFindFile(fileName, Integer.parseInt(bounces));
                    System.out.printf("%s - %s %s\n", p.getName(), p.getAddress(), p.getPort());
                }

                case "download" -> {
                    String fileName = args[1];
                    int bounces = Integer.parseInt(args[2]);

                    try {
                        byte[] res = peer.sendDownload(fileName, bounces);
                        System.out.println("Downloaded a total of " + res.length + " bytes of data and saved them.");
                    } catch (FileNotFoundException e) {
                        System.out.println("File not found.");
                    }
                }

                default -> System.out.println("Unknown command. Type help to see the list of commands.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("Wrong arguments for command [%s]\n", command);
        } catch (IOException e) {
            System.out.printf("Error while executing command [%s], are you sure the peer is running?\n", command);
        }
    }

}
