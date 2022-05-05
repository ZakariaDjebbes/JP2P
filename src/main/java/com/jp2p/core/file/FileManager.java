package com.jp2p.core.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager {
    private String path;
    private File[] files;

    public FileManager(String path) {
        this.path = path;
        lookupFiles();
    }

    public void lookupFiles(){
        File curDir = new File(path);

        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        files = curDir.listFiles();
    }

    public byte[] getBytes(String fileName) throws IOException {
        for(File f: files){
            if(f.getName().equals(fileName)){
                return Files.readAllBytes(f.toPath());
            }
        }

        throw new FileNotFoundException("File not found: " + fileName);
    }

    public void writeFile(String fileName, byte[] data) throws IOException {
        File f = new File(path + File.separator + fileName);
        Files.write(f.toPath(), data);
    }

    public File getFile(String fileName) throws FileNotFoundException{
        for(File f: files){
            if(f.getName().equals(fileName)){
                return f;
            }
        }

        throw new FileNotFoundException("File not found: " + fileName);
    }

    public File[] getFiles() {
        return files;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        lookupFiles();
    }
}
