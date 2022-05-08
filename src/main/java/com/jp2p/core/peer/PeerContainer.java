package com.jp2p.core.peer;

import com.jp2p.core.exceptions.PeerOverflowException;

import java.util.ArrayList;

/**
 * This is a container for the known {@link Peer}s of a peer.
 * There can be a maximum number {@link PeerContainer#maxPeers} of peers.
 * Provides utility methods to interact with the list of known {@link Peer}s.
 */
public class PeerContainer {
    /**
     * The maximum number of {@link Peer}s that a peer can know of and thus that can be stored in the container.
     */
    private final int maxPeers;

    /**
     * The list of known {@link Peer}s.
     */
    private final ArrayList<Peer> peers;

    /**
     * Constructs a new {@link PeerContainer} with the given maximum number of {@link Peer}s that can be known of.
     *
     * @param maxPeers The maximum number of {@link Peer}s that can be stored.
     */
    public PeerContainer(int maxPeers) {
        this.maxPeers = maxPeers;
        this.peers = new ArrayList<>();
    }

    /**
     * Returns the {@link Peer} with the given name.
     * This method is synchronized to avoid concurrent access to the list of {@link PeerContainer#peers}.
     *
     * @param name The name of the {@link Peer} to return.
     * @return The {@link Peer} with the given name.
     */
    public synchronized Peer getPeer(String name) {
        return peers.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Adds the given {@link Peer} to the list of known {@link PeerContainer#peers}.
     * This method is synchronized to avoid concurrent access to the list of {@link PeerContainer#peers}.
     *
     * @param peer The {@link Peer} to add to {@link PeerContainer#peers}.
     * @return True if the {@link Peer} was added, false otherwise (Meaning a peer with the same name already exists).
     * @throws PeerOverflowException If the number of known {@link Peer}s exceeds {@link PeerContainer#maxPeers}.
     */
    public synchronized boolean addPeer(Peer peer) throws PeerOverflowException {
        if (peers.size() == maxPeers)
            throw new PeerOverflowException("Peer overflow");

        for (Peer p : peers) {
            if (peer.getName().equals(p.getName())) {
                return false;
            }
        }

        peers.add(peer);
        return true;
    }

    /**
     * Removes a {@link Peer} from the list of known {@link PeerContainer#peers} given its name.
     * This method is synchronized to avoid concurrent access to the list of {@link PeerContainer#peers}.
     *
     * @param name The name of the {@link Peer} to remove.
     * @return True if the {@link Peer} with the given name was removed, false otherwise.
     */
    public synchronized boolean removePeer(String name) {
        return peers.removeIf(p -> p.getName().equals(name));
    }

    /**
     * Returns the list of known {@link Peer}s.
     * This method is synchronized to avoid concurrent access to the list of {@link PeerContainer#peers}.
     *
     * @return The list of known {@link Peer}s.
     */
    public synchronized ArrayList<Peer> getPeers() {
        return peers;
    }
}
