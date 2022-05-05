package com.jp2p.core.exceptions;

import java.io.Serial;

public class PeerOverflowException extends Exception {

    @Serial
    private static final long serialVersionUID = -82326145915673025L;

    public PeerOverflowException(String message) {
        super(message);
    }
}
