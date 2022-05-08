package com.jp2p.core.peer;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Represents a peer in the network. Stores the peer's name, IP address, and port.
 */
public class Peer {
    /**
     * The name of the peer.
     */
    private String name;

    /**
     * The IP address of the peer.
     */
    private String address;

    /**
     * The port of the peer.
     */
    private int port;

    /**
     * Creates a new peer with the given name, address, and port.
     *
     * @param name    The name of the peer.
     * @param address The IP address of the peer.
     * @param port    The port of the peer.
     */
    public Peer(String name, String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    /**
     * Creates a new peer with the given name and port at the localhost address.
     *
     * @param name The name of the peer.
     * @param port The port of the peer.
     * @throws UnknownHostException If the localhost address cannot be found.
     */
    public Peer(String name, int port) throws UnknownHostException {
        this.name = name;
        this.port = port;
        this.address = Inet4Address.getLocalHost().getHostAddress();
    }

    /**
     * Returns the name of the peer.
     *
     * @return The name of the peer.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the peer.
     *
     * @param name The new name of the peer.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the IP address of the peer.
     *
     * @return The IP address of the peer.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the IP address of the peer.
     *
     * @param address The new IP address of the peer.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the port of the peer.
     *
     * @return The port of the peer.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port of the peer.
     */
    public void setPort(int port) {
        this.port = port;
    }
}
