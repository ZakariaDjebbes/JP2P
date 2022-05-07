package com.jp2p.core.commands;

import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerRunner;

import java.io.IOException;
import java.net.Socket;


/**
 * The command to close the connection with the peer, meaning removing the peer from the list of peers.
 * The implementation of the bye message.
 */
public record ByeCommand(PeerRunner peerRunner) implements ICommand {
    @Override
    public Object execute(Object... args) {
        peerRunner.getPeerContainer().removePeer((String) args[0]);
        for (Peer p : peerRunner.getPeerContainer().getPeers()) {
            try {
                peerRunner.sendBye(new Socket(p.getAddress(), p.getPort()));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "Successfully disconnected. Bye!";
    }
}
