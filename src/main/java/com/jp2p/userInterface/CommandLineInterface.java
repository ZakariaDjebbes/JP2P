package com.jp2p.userInterface;

import com.jp2p.core.exceptions.NoKnownPeersException;
import com.jp2p.core.exceptions.PeerNotFoundException;
import com.jp2p.core.file.PeerFile;
import com.jp2p.core.peer.PeerRunner;
import com.jp2p.database.DatabaseConnection;
import com.jp2p.database.PeerConfigurationTable;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.sql.SQLException;
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
            DatabaseConnection.connect();
            PeerConfigurationTable.createTableIfNotExists();
            PeerConfigurationTable.seedConfiguration();
            peer = PeerRunner.startUp();
            new Thread(peer).start();
            Thread.sleep(500);
            runCommandLineInterface();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error starting up the peer. Are you sure the provided port is open? \nExiting...");
        } catch (ExceptionInInitializerError e) {
            System.out.println("Error loading the seed file, are you sure the seed file is correctly set in resources/seed.json? \nExiting...");
        } catch (SQLException e) {
            System.out.println("Database connection failed. Are you sure the database file is in the right place?");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                    builder.append("\tID\t \tFile Name\t \tFile Size\t \tPeer Name\t \tDownloaded\t \n");
                    for (int i = 0; i < files.size(); i++) {
                        builder.append(String.format("\t%s.\t \t%s\t \t%09d\t \t%s\t \t%s\t \n",
                                i,
                                files.get(i).getFileName(),
                                files.get(i).getFileSize(),
                                files.get(i).getPeerName(),
                                files.get(i).getWasDownloaded() ? "Yes" : "No"));
                    }
                    System.out.println(builder);
                    System.out.print("Enter the ID of the file you want to download (or -1 to cancel): ");
                    int index = new Scanner(System.in).nextInt();

                    if (index == -1) {
                        System.out.println("Canceling...");
                        return;
                    }

                    System.out.println("Downloading file...");
                    if (peer.sendDownload(index))
                        System.out.printf("Downloaded %s bytes and saved them.%n\n", files.get(index).getFileSize());
                    else
                        System.out.printf("Download failed. Downloaded a total of %s bytes when expecting %s bytes.\n",
                                files.get(index).getDownloadedSize(),
                                files.get(index).getFileSize());
                }

                default -> System.out.println("Unknown command. Type help to see the list of commands.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.printf("Wrong arguments for command [%s]\n", command);
        } catch (NoKnownPeersException e) {
            System.out.println("This Peer doesn't know any other peer for now. Add peers first.");
        } catch (ConnectException | PeerNotFoundException e) {
            System.out.println("You are trying to interact with a peer that is not connected to the network.");
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
