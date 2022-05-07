package com.jp2p.core.commands;

import com.jp2p.core.file.FileManager;
import com.jp2p.core.file.PeerFile;

/**
 * The command to add a file and the peer that has it to the list of files discovered in the network (To be downloaded later).
 * The implementation of the voila message.
 */
public record VoilaCommand(FileManager fileManager) implements ICommand {
    @Override
    public Object execute(Object... args) {
        String peerName = (String) args[0];
        int numberOfFilesFound = Integer.parseInt((String) args[1]);

        for (int i = 0; i < numberOfFilesFound; i++) {
            String fileName = (String) args[i + 2];
            fileManager.addFile(new PeerFile(peerName, fileName));
        }

        return "done";
    }
}