package com.jp2p.core.commands;

import com.jp2p.core.file.FolderManger;

import java.io.*;

/**
 * The command to download a file from a remote peer.
 * The implementation of the download message.
 * The message received is in this format: download [file name] [skip bytes]
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public record DownloadCommand(FolderManger fileManager) implements ICommand {
    @Override
    public Object execute(Object... args) {
        String fileName = (String) args[0];
        int skipBytes = Integer.parseInt((String) args[1]);
        ObjectOutputStream out = (ObjectOutputStream) args[2];

        try {
            File file = fileManager.getFile(fileName);
            out.writeLong(file.length());
            out.flush();

            BufferedInputStream stream = fileManager.getAsInStream(fileName);
            stream.skip(skipBytes);
            int read;
            byte[] bytes = new byte[2048];

            while ((read = stream.read(bytes)) > 0) {
                out.write(bytes, 0, read);
                out.flush();
            }

            stream.close();
        } catch (FileNotFoundException e) {
            return "File not found";
        } catch (IOException e) {
            return "Error reading file";
        }

        return "done";
    }
}
