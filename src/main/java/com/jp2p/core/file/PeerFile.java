package com.jp2p.core.file;

public class PeerFile {
    private final String peerName;
    private final String fileName;
    private boolean wasDownloaded;

    public PeerFile(String peerName, String fileName) {
        this.peerName = peerName;
        this.fileName = fileName;
        this.wasDownloaded = false;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPeerName() {
        return peerName;
    }

    public boolean wasDownloaded() {
        return wasDownloaded;
    }

    public void setDownloaded(boolean b) {
        this.wasDownloaded = b;
    }
}
