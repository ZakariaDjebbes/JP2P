package com.jp2p.userInterface;

import com.jp2p.core.exceptions.NoKnownPeersException;
import com.jp2p.core.exceptions.PeerNotFoundException;
import com.jp2p.core.file.PeerFile;
import com.jp2p.core.peer.PeerRunner;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class is a simple command line interface to interact with the system.
 */
@SuppressWarnings("InfiniteLoopStatement")
public class CommandLineInterface {
    public static PeerRunner peer;

    public static void main(String[] args) {
        try {
            peer = PeerRunner.startUp();
            new Thread(peer).start();
            Thread.sleep(500);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        runCommandLineInterface();
    }

    /**
     * This method is the main loop of the command line interface.
     * Reads a command, executes it and repeats.
     */
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

    /**
     * This method invokes the command specified by the user by calling the relevant method of the {@link PeerRunner}.
     *
     * @param command The command to be invoked.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
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
                    peer.sendFindFile(fileName, Integer.parseInt(bounces));
                    System.out.printf("Finding file %s in the network with %s bounces...\n", fileName, bounces);
                }

                case "download" -> {
                    ArrayList<PeerFile> files = peer.getFilesFoundManager().getFilesFound();
                    if (files.size() == 0) {
                        System.out.println("No files found yet, use the file command to find files on the network.");
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    builder.append(String.format("Found %s files:\n", files.size()));
                    builder.append("ID \t File name \t Peer \t Was Downloaded \n");
                    for (int i = 0; i < files.size(); i++) {
                        builder.append(String.format("%s. \t%s \t%s \t%s \n", i, files.get(i).getFileName(), files.get(i).getPeerName(), files.get(i).wasDownloaded() ? "Yes" : "No"));
                    }
                    System.out.println(builder);
                    System.out.print("Enter the ID of the file you want to download (or -1 to cancel): ");
                    int index = new Scanner(System.in).nextInt();

                    if (index == -1) {
                        System.out.println("Canceling...");
                        return;
                    }

                    System.out.println("Downloading file...");
                    long res = peer.sendDownload(index);
                    System.out.printf("Downloaded %s bytes and saved them.%n\n", res);
                }

                default -> System.out.println("Unknown command. Type help to see the list of commands.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("Wrong arguments for command [%s]\n", command);
        } catch (NoKnownPeersException e) {
            System.out.println("This Peer doesn't know any other peer for now. Add peers first.");
        } catch (ConnectException | PeerNotFoundException e) {
            System.out.println("You are trying to interact with a peer that is no longer connected to the network.");
        } catch (IOException e) {
            System.out.println("An error occurred while interacting with the network. Are you sure the peer is still connected?");
        }
    }

    /**
     * Prints the list of commands available to the user.
     */
    private static void printHelp() {
        System.out.println("""
                List of commands (type help to see it again or clear to clear the console):
                - name [hostname] [port number] : Asks the peer at the specified hostname and port number to send its name.
                - knownPeers [hostname] [port number] : Gets the list of known peers of the peer at the specified hostname and port number.
                - itsMe [hostname] [port number] : Tells the peer at the specified hostname and port number that you are a peer.
                - file [fileName.ext] [bounces] : Find the file in the network.
                - download : Choose a file previously found with the file command to download from a peer.
                - bye [hostname] [port number] : Tells the peer at the specified hostname and port number that you are leaving the system thus removing you from its known peers.
                """);
    }
}
