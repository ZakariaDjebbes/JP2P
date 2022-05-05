package com.jp2p.core.commands;

import com.jp2p.core.file.FileManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;

public record DownloadCommand(FileManager fileManager) implements ICommand{

    @Override
    public Object execute(Object... args) {
        String fileName = (String)args[0];
        ObjectOutputStream out = (ObjectOutputStream) args[1];

        try {
            byte[] data = fileManager.getBytes(fileName);
            out.writeInt(data.length);
            out.write(data);

//            for (byte b : data) {
//                out.write(b);
//            }

            out.flush();
        }catch (FileNotFoundException e){
            return "File not found";
        }
        catch (IOException e) {
            return "Error reading file";
        }

        return "done";
    }
}
