package com.jp2p.core.file;

import java.util.ArrayList;

/**
 * The file manager is a container for files that have been discovered with the file message by the peer.
 * It stores them as a list of {@link PeerFile}s. And provides utility methods to interact with the list.
 */
public class FileManager {
    /**
     * The list of {@link PeerFile} that have been discovered by the peer.
     */
    private final ArrayList<PeerFile> filesFound;

    /**
     * Constructs an empty {@link FileManager}.
     */
    public FileManager() {
        filesFound = new ArrayList<>();
    }

    /**
     * Adds a {@link PeerFile} to the list.
     * If a file with the same name and peer is already in the list, it is not added again.
     * This method is synchronized to avoid concurrent access to the list of {@link FileManager#filesFound}.
     *
     * @param file The {@link PeerFile} to add to {@link FileManager#filesFound}.
     */
    public synchronized void addFile(PeerFile file) {
        for (PeerFile f : filesFound) {
            // Prevent duplicates
            if (f.getFileName().equals(file.getFileName()) && f.getPeerName().equals(file.getPeerName())) {
                return;
            }
        }

        filesFound.add(file);
    }

    /**
     * Returns the {@link PeerFile} at the given index in the {@link FileManager#filesFound}.
     * This method is synchronized to avoid concurrent access to the list of {@link FileManager#filesFound}.
     *
     * @param index The index of the {@link PeerFile} to return.
     * @return The {@link PeerFile} at the given index in the {@link FileManager#filesFound}.
     */
    public synchronized PeerFile getPeerNameAt(int index) {
        return filesFound.get(index);
    }

    /**
     * Returns the list of {@link PeerFile}s.
     * This method is synchronized to avoid concurrent access to the list of {@link FileManager#filesFound}.
     *
     * @return The list of {@link PeerFile}s in {@link FileManager}.
     */
    public synchronized ArrayList<PeerFile> getFilesFound() {
        return filesFound;
    }
}
