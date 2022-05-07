package com.jp2p.core.exceptions;

/**
 * This exception is thrown when a peer in the given list of found files is not found in the list of known peers.
 * This means that the peer has left the system.
 */
public class PeerNotFoundException extends Exception {

    /**
     * Constructs a new {@link PeerNotFoundException}.
     */
    public PeerNotFoundException() {
        super();
    }
}
