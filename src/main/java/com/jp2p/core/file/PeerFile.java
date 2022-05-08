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
     * The size of the file in bytes.
     */
    private final int fileSize;

    /**
     * The number of bytes downloaded by the peer following a download command.
     */
    private int downloadedSize;

    /**
     * Constructs a new {@link PeerFile}.
     *
     * @param peerName The name of the peer that has the discovered file.
     * @param fileName The name of the discovered file.
     * @param fileSize The size of the file in bytes.
     */
    public PeerFile(String peerName, String fileName, int fileSize) {
        this.peerName = peerName;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.downloadedSize = 0;
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
    public boolean getWasDownloaded() {
        return wasDownloaded;
    }

    /**
     * Sets the status of the file.
     *
     * @param wasDownloaded True if the file was downloaded by the peer, false otherwise.
     */
    public void setWasDownloaded(boolean wasDownloaded) {
        this.wasDownloaded = wasDownloaded;
    }

    /**
     * A getter for the size of the file in bytes.
     *
     * @return The size of the file in bytes.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the number of bytes downloaded by the peer following a download command.
     *
     * @param downloadedSize The number of bytes downloaded by the peer following a download command.
     */
    public void setDownloadedSize(int downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    /**
     * A getter for the number of bytes downloaded by the peer following a download command.
     *
     * @return The number of bytes downloaded by the peer following a download command.
     */
    public int getDownloadedSize() {
        return downloadedSize;
    }
}
