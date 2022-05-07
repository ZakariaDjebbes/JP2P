package com.jp2p.core.peer;

import com.jp2p.core.commands.*;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This is a slave of the {@link PeerRunner} class.
 * This class is responsible for handling the new connections and handle the messages / protocols of the system.
 */
@SuppressWarnings("InfiniteLoopStatement")
public class PeerTask implements Runnable {
    /**
     * The socket of the connection.
     */
    private final Socket client;

    /**
     * The {@link PeerRunner} that created this task.
     */
    private final PeerRunner me;

    /**
     * The {@link CommandExecutor} that will store and execute the {@link ICommand}s provided by the peer.
     */
    private final CommandExecutor commandExecutor;

    /**
     * Constructs a new {@link PeerTask} with the provided {@link Socket} and {@link PeerRunner}.
     *
     * @param client The {@link Socket} of the connection.
     * @param me     The master {@link PeerRunner} that created this task.
     */
    public PeerTask(Socket client, PeerRunner me) {
        this.client = client;
        this.me = me;
        commandExecutor = new CommandExecutor();
        addCommands();
    }

    /**
     * Adds the commands that will be executed by the {@link PeerTask#commandExecutor} whenever a message is received from a Client.
     *
     * @see CommandExecutor
     * @see CommandType
     * @see ICommand
     */
    private void addCommands() {
        commandExecutor.addCommand(CommandType.NAME, args -> me.getName());
        commandExecutor.addCommand(CommandType.KNOWN_PEERS, new KnownPeersCommand(me.getPeerContainer()));
        commandExecutor.addCommand(CommandType.ITS_ME, new ItsMeCommand(me.getPeerContainer()));
        commandExecutor.addCommand(CommandType.FILE, new FileCommand(me));
        commandExecutor.addCommand(CommandType.DOWNLOAD, new DownloadCommand(me.getFilesFolderManager()));
        commandExecutor.addCommand(CommandType.VOILA, new VoilaCommand(me.getFilesFoundManager()));
        commandExecutor.addCommand(CommandType.BYE, new ByeCommand(me));
    }

    /**
     * Runs the task.
     * Receives the messages from the client and executes the commands.
     * All commands received should follow the following format: <command>(!|?) <arg1> <arg2> <arg3>...
     */
    @Override
    public void run() {
        ObjectInputStream in;
        ObjectOutputStream out;
        try {
            in = new ObjectInputStream(client.getInputStream());
            out = new ObjectOutputStream(client.getOutputStream());

            while (true) {
                String command = (String) in.readObject();
                handleCommand(command, out);
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }
    }

    /**
     * Handles the command received from the client. It will execute the command using the {@link CommandExecutor} and send the result back to the client if required.
     * All commands received should follow the following format: <command>(!|?) <arg1> <arg2> <arg3>...
     *
     * @param command The command received from the client.
     * @param out     The {@link ObjectOutputStream} that will be used to send the result back to the client.
     * @throws IllegalArgumentException If the command is not valid, meaning the expected arguments or the expected form of the command aren't correct.
     * @throws IOException              If the {@link ObjectOutputStream} fails to send the result back to the client.
     */
    private void handleCommand(String command, ObjectOutputStream out) throws IllegalArgumentException, IOException {
        Pair<String, String[]> args = getArgs(command);
        switch (args.getKey()) {
            case "it's me" -> out.writeObject(commandExecutor.executeCommand(CommandType.ITS_ME, args.getValue()[0], args.getValue()[1], args.getValue()[2]));
            case "known peers" -> out.writeObject(commandExecutor.executeCommand(CommandType.KNOWN_PEERS));
            case "name" -> out.writeObject(commandExecutor.executeCommand(CommandType.NAME));
            case "file" -> commandExecutor.executeCommand(CommandType.FILE, args.getValue()[0], args.getValue()[1], args.getValue()[2], args.getValue()[3]);
            case "download" -> commandExecutor.executeCommand(CommandType.DOWNLOAD, args.getValue()[0], out);
            case "voila" -> commandExecutor.executeCommand(CommandType.VOILA, args.getValue());
            case "bye" -> out.writeObject(commandExecutor.executeCommand(CommandType.BYE, args.getValue()[0]));
        }
    }

    /**
     * Splits the command the received from the client into the command and the arguments.
     * The characters "!" and "?" are used to split the command and the arguments.
     * All commands received should follow the following format: <command>(!|?) <arg1> <arg2> <arg3>...
     *
     * @param command The command received from the client.
     * @return A {@link Pair} containing the command and the arguments.
     * @throws IllegalArgumentException If the command is not valid, meaning the expected arguments or the expected form for the command aren't correct.
     */
    private Pair<String, String[]> getArgs(String command) throws IllegalArgumentException {
        if (command.contains("!") || command.contains("?")) {
            command = command.replace("?", "!");
            String[] split = command.split("!");

            return split.length > 1 ? new Pair<>(split[0], split[1].trim().split(" ")) : new Pair<>(split[0], null);
        } else
            throw new IllegalArgumentException("Provided arguments don't match the format [messageName(?|!) arg1 arg2 ...]");
    }
}
