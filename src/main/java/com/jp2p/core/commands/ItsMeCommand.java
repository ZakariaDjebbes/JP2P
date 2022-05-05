package com.jp2p.core.commands;

import com.jp2p.core.exceptions.PeerOverflowException;
import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerContainer;

public record ItsMeCommand(PeerContainer peerContainer) implements ICommand {

    @Override
    public String execute(Object... args) {
        try {
            return peerContainer.addPeer(new Peer((String) args[0], (String) args[1], Integer.parseInt((String) args[2]))) ? "done" : "this peer is already in the list";
        } catch (PeerOverflowException e) {
            return "error, peer list full";
        }
    }
}
