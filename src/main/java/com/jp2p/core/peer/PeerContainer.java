package com.jp2p.core.peer;

import com.jp2p.core.exceptions.PeerOverflowException;

import java.util.ArrayList;

public class PeerContainer {
    private final int maxPeers;
    private final ArrayList<Peer> peers;

    public PeerContainer(int maxPeers) {
        this.maxPeers = maxPeers;
        this.peers = new ArrayList<>();
    }

    public Peer getPeer(String name) {
        return peers.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    public boolean addPeer(Peer peer) throws PeerOverflowException {
        if(peers.size() == maxPeers)
            throw new PeerOverflowException("Peer overflow");

        for(Peer p : peers) {
            if(peer.getName().equals(p.getName())) {
              return false;
            }
        }

        peers.add(peer);
        return true;
    }

    public void removePeer(String name) {
        peers.stream().filter(p -> p.getName().equals(name)).findFirst().ifPresent(peers::remove);
    }

    public ArrayList<Peer> getPeers() {
        return peers;
    }
}
