package com.jp2p.core.peer;

import com.jp2p.core.file.FileManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerRunner implements Runnable {
    public static final String PEER_FILE_PATH = "./files/";
    public static final String PEER_DOWNLOADS_PATH = "./downloads/";
    public static final int MAX_THREADS = 10;
    private final UUID peerId;
    private final int port;
    private final ServerSocket socket;
    private final ExecutorService executor;
    private String name;
    private final PeerContainer peerContainer;
    private final FileManager filesManager;
    private final FileManager downloadsManager;

    public PeerRunner(String name, int port, int maxPeers) throws IOException {
        this.name = name;
        this.port = port;
        this.peerContainer = new PeerContainer(maxPeers);
        this.peerId = UUID.randomUUID();
        this.filesManager = new FileManager(PEER_FILE_PATH);
        this.downloadsManager = new FileManager(PEER_DOWNLOADS_PATH);

        this.socket = new ServerSocket(port);
        this.executor = Executors.newFixedThreadPool(MAX_THREADS);
    }

    @Override
    public void run() {
        System.out.printf("Peer [%s] with id [%s] up and listening for other peers on port [%s]...%n", name, peerId, port);
        try {
            while (true) {
                this.executor.execute(new PeerTask(socket.accept(), this));
            }
        } catch (IOException E) {
            E.printStackTrace();
        }
    }

    public void stop() throws IOException {
        socket.close();
    }

    public String sendGetName(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject("name?");
        return (String) in.readObject();
    }

    public String sendGetKnownPeers(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject("known peers?");
        return (String) in.readObject();
    }


    public String sendItsMe(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("it's me! %s %s %s", this.name, Inet4Address.getLocalHost().getHostAddress(), this.port));
        return (String) in.readObject();
    }

    public Peer sendFindFile(String fileName, int bounces) throws IOException, ClassNotFoundException {
        for (Peer p : this.peerContainer.getPeers()) {
            Socket socket = new Socket(p.getAddress(), p.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(String.format("file? %s %s", fileName, bounces));
            String res = (String) in.readObject();

            if(res.equals("voila")){
                return p;
            }
        }

        throw new FileNotFoundException("Couldn't find the file [" + fileName + "]");
    }

    public byte[] sendDownload(String fileName, int bounces) throws IOException, ClassNotFoundException {
        Peer peer = sendFindFile(fileName, bounces);
        Socket socket = new Socket(peer.getAddress(), peer.getPort());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("download? %s", fileName));
        int size = in.readInt();

        System.out.println("Size: " + size);

        byte[] file = new byte[size];

        for (int i = 0; i < size; i++) {
            file[i] = (byte) in.read();
        }

        downloadsManager.writeFile(fileName, file);

        return file;
    }

    public String sendBye(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("bye! %s", this.name));
        return (String) in.readObject();
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getPeerId() {
        return peerId;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public PeerContainer getPeerContainer() {
        return peerContainer;
    }

    public FileManager getFilesManager() {
        return filesManager;
    }

    public FileManager getDownloadsManager() {
        return downloadsManager;
    }
}
