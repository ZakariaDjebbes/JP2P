package com.jp2p.core.peer;

import com.jp2p.core.commands.*;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@SuppressWarnings("InfiniteLoopStatement")
public class PeerTask implements Runnable {
    private final Socket client;
    private final PeerRunner me;
    private final CommandExecutor commandExecutor;

    public PeerTask(Socket client, PeerRunner me) {
        this.client = client;
        this.me = me;
        commandExecutor = new CommandExecutor();
        addCommands();
    }

    private void addCommands() {
        commandExecutor.addCommand(CommandType.NAME, args -> me.getName());
        commandExecutor.addCommand(CommandType.KNOWN_PEERS, new KnownPeersCommand(me.getPeerContainer()));
        commandExecutor.addCommand(CommandType.ITS_ME, new ItsMeCommand(me.getPeerContainer()));
        commandExecutor.addCommand(CommandType.FILE, new FileCommand(me));
        commandExecutor.addCommand(CommandType.DOWNLOAD, new DownloadCommand(me.getFilesFolderManager()));
        commandExecutor.addCommand(CommandType.VOILA, new VoilaCommand(me.getFilesFoundManager()));
        commandExecutor.addCommand(CommandType.BYE, new ByeCommand(me));
    }

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

    private Pair<String, String[]> getArgs(String command) throws IllegalArgumentException {
        if (command.contains("!") || command.contains("?")) {
            command = command.replace("?", "!");
            String[] split = command.split("!");

            return split.length > 1 ? new Pair<>(split[0], split[1].trim().split(" ")) : new Pair<>(split[0], null);
        } else
            throw new IllegalArgumentException("Provided arguments don't match the format [messageName(?|!) arg1 arg2 ...]");
    }
}
