package com.jp2p.core.commands;

import com.jp2p.core.file.FolderManger;

import java.io.*;

public record DownloadCommand(FolderManger fileManager) implements ICommand {
    @Override
    public Object execute(Object... args) {
        String fileName = (String)args[0];
        ObjectOutputStream out = (ObjectOutputStream) args[1];

        try {
            File file = fileManager.getFile(fileName);
            out.writeLong(file.length());
            out.flush();

            BufferedInputStream stream = fileManager.getAsInStream(fileName);
            int read;
            byte[] bytes = new byte[2048];

            while ((read = stream.read(bytes)) > 0) {
                out.write(bytes, 0, read);
                out.flush();
            }

            stream.close();
        } catch (FileNotFoundException e){
            return "File not found";
        } catch (IOException e) {
            return "Error reading file";
        }

        return "done";
    }
}
