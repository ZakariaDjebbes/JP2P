package com.jp2p.core.exceptions;

import java.io.Serial;

public class NoKnownPeersException extends Throwable {

    @Serial
    private static final long serialVersionUID = -82326145915673025L;

    public NoKnownPeersException(String message) {
        super(message);
    }
}
