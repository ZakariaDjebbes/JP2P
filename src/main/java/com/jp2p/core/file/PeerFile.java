package com.jp2p.core.file;

/**
 * The {@link PeerFile} class is a wrapper for a file that has been discovered by a peer on the system.
 * Contains the file name and the name of the peer from which the file can be downloaded alongside other informations.
 */
public class PeerFile {
    /**
     * The name of the peer that has the discovered file.
     */
    private final String peerName;

    /**
     * The name of the discovered file.
     */
    private final String fileName;

    /**
     * Was the file downloaded by the peer at some point?
     */
    private boolean wasDownloaded;

    /**
     * Constructs a new {@link PeerFile}.
     *
     * @param peerName The name of the peer that has the discovered file.
     * @param fileName The name of the discovered file.
     */
    public PeerFile(String peerName, String fileName) {
        this.peerName = peerName;
        this.fileName = fileName;
        this.wasDownloaded = false;
    }

    /**
     * A getter for the name of the discovered file.
     *
     * @return The name of the discovered file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * A getter for the name of the peer that has the discovered file.
     *
     * @return The name of the peer that has the discovered file.
     */
    public String getPeerName() {
        return peerName;
    }

    /**
     * A getter for the status of the file.
     *
     * @return True if the file was downloaded by the peer, false otherwise.
     */
    public boolean wasDownloaded() {
        return wasDownloaded;
    }

    /**
     * Sets the status of the file.
     *
     * @param wasDownloaded True if the file was downloaded by the peer, false otherwise.
     */
    public void setDownloaded(boolean wasDownloaded) {
        this.wasDownloaded = wasDownloaded;
    }
}
