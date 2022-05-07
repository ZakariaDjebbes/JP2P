package com.jp2p.core.exceptions;

import java.io.Serial;

/**
 * This exception is thrown when there are no known peers on the network and the user tries to use the file message.
 */
public class NoKnownPeersException extends Throwable {

    @Serial
    private static final long serialVersionUID = -82326145915673025L;

    /**
     * Constructs a new {@link NoKnownPeersException} with the specified detail message.
     *
     * @param message the detail message of the exception.
     */
    public NoKnownPeersException(String message) {
        super(message);
    }
}
