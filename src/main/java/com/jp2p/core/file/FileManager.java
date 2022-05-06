package com.jp2p.core.file;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager {
    private String path;
    private File[] files;

    public FileManager(String path) {
        this.path = path;
        lookupFiles();
    }

    public void lookupFiles() {
        File curDir = new File(path);

        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        files = curDir.listFiles();
    }

    public BufferedInputStream getAsInStream(String fileName) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(path + fileName));
    }

    public BufferedOutputStream getAsOutStream(String fileName) throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(path + fileName));
    }

    public File getFile(String fileName) throws FileNotFoundException {
        for (File f : files) {
            if (f.getName().equals(fileName)) {
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
