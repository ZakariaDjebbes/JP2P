package com.jp2p.core.file;

import java.util.ArrayList;

public class FileManager {
    private final ArrayList<PeerFile> filesFound;

    public FileManager() {
        filesFound = new ArrayList<>();
    }

    public void addFile(PeerFile file) {
        for (PeerFile f : filesFound) {
            // Prevent duplicates
            if (f.getFileName().equals(file.getFileName()) && f.getPeerName().equals(file.getPeerName())) {
                return;
            }
        }

        filesFound.add(file);
    }

    public PeerFile getPeerNameAt(int index) {
        return filesFound.get(index);
    }

    public ArrayList<PeerFile> getFilesFound() {
        return filesFound;
    }
}
