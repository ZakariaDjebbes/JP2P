package com.jp2p.tui;

import com.jp2p.core.exceptions.NoKnownPeersException;
import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("InfiniteLoopStatement")
public class CommandLineInterface {
    public static PeerRunner peer;

    public static void main(String[] args) {
        try {
            peer = PeerRunner.Startup();
            new Thread(peer).start();
            Thread.sleep(500);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        runCommandLineInterface();
    }

    private static void runCommandLineInterface() {
        printHelp();
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
                case "help" -> printHelp();

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
                    new Thread(() -> {
                        try {
                            Peer p = peer.sendFindFile(fileName, Integer.parseInt(bounces)).get();
                            if (p == null)
                                print("The requested file was not found.");
                            else
                                print(String.format("%s - %s %s", p.getName(), p.getAddress(), p.getPort()));
                        } catch (SocketTimeoutException e) {
                            print(String.format("The request timed out with out a response. The requested file [%s] was not found.", fileName));
                        } catch (FileNotFoundException e) {
                            print("The requested file was not found.");
                        } catch (IOException ignored) {
                            print(String.format("Error while executing command [%s], are you sure the peer is running?", command));
                        } catch (ExecutionException | InterruptedException e) {
                            print("Concurrency error while executing command.");
                            e.printStackTrace();
                        } catch (NoKnownPeersException e) {
                            print("No known peers to download the file. Add peers frist.");
                        }
                    }).start();
                }

                case "download" -> {
                    String fileName = args[1];
                    int bounces = Integer.parseInt(args[2]);
                    new Thread(() -> {
                        try {
                            long res = peer.sendDownload(fileName, bounces);
                            print("Downloaded a total of " + res + " bytes of data and saved them.");
                        } catch (SocketTimeoutException e) {
                            print(String.format("The request timed out with out a response. The requested file [%s] was not found.", fileName));
                        } catch (FileNotFoundException e) {
                            print("The requested file was not found.");
                        } catch (IOException ignored) {
                            print(String.format("Error while executing command [%s], are you sure the peer is running?", command));
                        } catch (ExecutionException | InterruptedException e) {
                            print("Concurrency error while executing command.");
                            e.printStackTrace();
                        } catch (NoKnownPeersException e) {
                            print("No known peers to download the file. Add peers frist.");
                        }
                    }).start();

                }

                default -> System.out.println("Unknown command. Type help to see the list of commands.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("Wrong arguments for command [%s]\n", command);
        }
    }

    private static void printHelp() {
        System.out.println("""
                        List of commands (type help to see it again or clear to clear)
                        1 - name [hostname] [port number]
                        2 - knownPeers [hostname] [port number]
                        3 - itsMe [hostname] [port number]
                        4 - file [fileName.ext] [bounces]
                        5 - download [fileName.ext] [bounces]
                        6 - bye [hostname] [port number]
                        """);
    }

    private static void print(String message) {
        System.out.printf("\n%s\n%s>", message, peer.getName());
    }
}
