package com.jp2p.core.commands;

import com.jp2p.core.exceptions.NoKnownPeersException;
import com.jp2p.core.peer.PeerRunner;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The command to look for a file in the network of peers.
 * The implementation of the file message.
 */
public record FileCommand(PeerRunner peerRunner) implements ICommand {
    @Override
    public Object execute(Object... args) {
        String fileName = (String) args[0];
        int bounces = Integer.parseInt((String) args[1]);
        int outputPort = Integer.parseInt((String) args[2]);
        String outputAddress = (String) args[3];

        try {
            if (bounces > 0) {
                ArrayList<File> files = peerRunner.getFilesFolderManager().getFiles(fileName);

                if (!files.isEmpty()) {
                    peerRunner.sendVoila(new Socket(outputAddress, outputPort), files);
                    return "done";
                }

                bounces--;
                peerRunner.sendFindFile(fileName, bounces);
            } else {
                return "failed";
            }
        } catch (Exception | NoKnownPeersException ignored) {
            // ignored, we don't care because it will be handled by the caller
        }

        return "done";
    }
}
