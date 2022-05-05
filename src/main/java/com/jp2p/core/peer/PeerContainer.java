package com.jp2p.core.peer;

import com.jp2p.core.exceptions.PeerOverflowException;

import java.util.ArrayList;

public class PeerContainer {
    private int maxPeers;
    private final ArrayList<Peer> peers;

    public PeerContainer(int maxPeers) {
        this.maxPeers = maxPeers;
        this.peers = new ArrayList<>();
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
        for(Peer p : peers) {
            if(p.getName().equals(name)) {
                peers.remove(p);
                return;
            }
        }
    }

    public void setMaxPeers(int maxPeers) {
        this.maxPeers = maxPeers;
    }

    public int getMaxPeers() {
        return maxPeers;
    }

    public ArrayList<Peer> getPeers() {
        return peers;
    }
}
