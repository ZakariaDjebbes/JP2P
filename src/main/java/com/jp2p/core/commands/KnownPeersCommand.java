package com.jp2p.core.commands;

import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerContainer;

/**
 * The command to ask a given peer to send its list of known peers.
 * The implementation of the known peers message.
 */
public record KnownPeersCommand(PeerContainer peerContainer) implements ICommand {

    @Override
    public Object execute(Object... args) {
        StringBuilder builder = new StringBuilder();

        if (peerContainer.getPeers().isEmpty()) {
            return "I have no known peers";
        }

        builder.append(String.format("I know %s peers :\n", peerContainer.getPeers().size()));
        builder.append("Name \tAddress \tPort \n");
        for (Peer p : peerContainer.getPeers()) {
            builder.append(String.format("%s \t%s \t%s\n", p.getName(), p.getAddress(), p.getPort()));
        }

        return builder.toString();
    }
}
