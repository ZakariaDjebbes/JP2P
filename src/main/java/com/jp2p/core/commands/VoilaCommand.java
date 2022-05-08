package com.jp2p.core.commands;

import com.jp2p.core.file.FileManager;
import com.jp2p.core.file.PeerFile;

/**
 * The command to add a file and the peer that has it to the list of files discovered in the network (To be downloaded later).
 * The implementation of the voila message.
 * The voila message receives the message in this format : <peer name> <number of files found> <file name 1> <file size1> <file name 2> <file size2>...
 */
public record VoilaCommand(FileManager fileManager) implements ICommand {
    @Override
    public Object execute(Object... args) {
        String peerName = (String) args[0];
        int numberOfFilesFound = Integer.parseInt((String) args[1]);

        // Since the format is <peer name> <number of files found> <file name 1> <file size1> <file name 2> <file size2>...
        // For each file I need to read the name and the size there for the step is 2 and there are total of 2 * args (after the removal of the peer name and the number of files found)
        for (int i = 0; i < numberOfFilesFound * 2; i += 2) {
            String fileName = (String) args[i + 2];
            int fileSize = Integer.parseInt((String) args[i + 3]);
            fileManager.addFile(new PeerFile(peerName, fileName, fileSize));
        }

        return "done";
    }
}
