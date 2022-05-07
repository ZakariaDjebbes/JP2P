package com.jp2p.core.commands;

import com.jp2p.core.exceptions.PeerOverflowException;
import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerContainer;

public record ItsMeCommand(PeerContainer peerContainer) implements ICommand {

    @Override
    public String execute(Object... args) {
        try {
            return peerContainer.addPeer(new Peer((String) args[0], (String) args[1], Integer.parseInt((String) args[2]))) ? "Successfully added to the list of peers, Welcome!" : "This peer is already in the list of peers.";
        } catch (PeerOverflowException e) {
            return "The list of peers is full.";
        }
    }
}
