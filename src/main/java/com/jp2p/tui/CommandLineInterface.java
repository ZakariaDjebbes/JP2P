package com.jp2p.tui;

import com.jp2p.configuration.ConfigurationReader;
import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.BindException;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

@SuppressWarnings("InfiniteLoopStatement")
public class CommandLineInterface {
    public static PeerRunner peer;

    public static void main(String[] args) {
        try {
            peer = PeerRunner.Startup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(peer).start();
        runCommandLineInterface();
    }

    private static void runCommandLineInterface() {
        while (true) {
            System.out.printf("%s>", peer.getName());
            String userCommand = new Scanner(System.in).nextLine();
            try {
                invokeCommand(userCommand);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void invokeCommand(String command) throws IOException, ClassNotFoundException {
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
                        long res = peer.sendDownload(fileName, bounces);
                        System.out.println("Downloaded a total of " + res + " bytes of data and saved them.");
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
