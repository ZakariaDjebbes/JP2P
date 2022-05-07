package com.jp2p.core.exceptions;

import java.io.Serial;

/**
 * This exception is thrown when the user tries to add a new known peer to the list of known peers, and the maximum number of known peers is already reached.
 */
public class PeerOverflowException extends Exception {

    @Serial
    private static final long serialVersionUID = -82326145915673025L;

    /**
     * Constructs a new {@link PeerOverflowException} with the specified detail message.
     *
     * @param message The detail message of the exception.
     */
    public PeerOverflowException(String message) {
        super(message);
    }
}
