package com.jp2p.core.peer;

import com.jp2p.configuration.ConfigurationReader;
import com.jp2p.core.exceptions.NoKnownPeersException;
import com.jp2p.core.file.FileManager;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.*;

@SuppressWarnings("InfiniteLoopStatement")
public class PeerRunner implements Runnable {
    public static final String PEER_FILE_PATH = "./files/";
    public static final String PEER_DOWNLOADS_PATH = "./downloads/";
    public static final int MAX_THREADS = 10;
    private final UUID peerId;
    private final int port;
    private final ServerSocket mainSocket, fileSocket;
    private final ExecutorService slavePool;
    private final String name;
    private final PeerContainer peerContainer;
    private final FileManager filesManager;
    private final FileManager downloadsManager;

    private PeerRunner(String name, int port, int maxPeers) throws IOException {
        this.name = name;
        this.port = port;
        this.peerContainer = new PeerContainer(maxPeers);
        this.peerId = UUID.randomUUID();
        this.filesManager = new FileManager(PEER_FILE_PATH);
        this.downloadsManager = new FileManager(PEER_DOWNLOADS_PATH);
        this.mainSocket = new ServerSocket(port); // Main server socket, this one will listen for other peers
        this.fileSocket = new ServerSocket(0); // File server socket, this one will listen for voila messages only
        this.slavePool = Executors.newFixedThreadPool(MAX_THREADS);
    }

    public static PeerRunner Startup() throws IOException {
        PeerRunner peer;
        String defaultName = ConfigurationReader.GetNode("default_peer", "name");
        int maxPeers = Integer.parseInt(ConfigurationReader.GetNode("default_peer", "max_peers"));
        int defaultPort = Integer.parseInt(ConfigurationReader.GetNode("default_peer", "port"));

        try {
            peer = new PeerRunner(defaultName, defaultPort , maxPeers);
            System.out.println("Because this is the first peer, It will always run on port " + defaultPort + " Go to the configuration file to change the default port.");
        } catch (BindException e) {
            System.out.print("Choose a name for this peer: ");
            String name = new Scanner(System.in).nextLine();
            System.out.print("Choose a port for this peer: ");
            int port = new Scanner(System.in).nextInt();
            peer = new PeerRunner(name, port , maxPeers);
        }

        return peer;
    }

    @Override
    public void run() {
        System.out.printf("Peer [%s] with id [%s] up and listening for other peers on port [%s]...%n", name, peerId, port);
        try {
            while (true) {
                this.slavePool.execute(new PeerTask(mainSocket.accept(), this));
            }
        } catch (IOException E) {
            E.printStackTrace();
        }
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

    public Future<Peer> sendFindFile(String fileName, int bounces) throws IOException, ExecutionException, InterruptedException, NoKnownPeersException {
        if(this.peerContainer.getPeers().size() == 0)
            throw new NoKnownPeersException("No known peers to search for file.");

        for (Peer p : this.peerContainer.getPeers()) {
            Socket socket = new Socket(p.getAddress(), p.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(String.format("file? %s %s %s %s", fileName, bounces, this.fileSocket.getLocalPort(), Inet4Address.getLocalHost().getHostAddress()));
        }

        Callable<Peer> task = () -> {
            try {
                Socket res = this.fileSocket.accept();
                res.setSoTimeout(Integer.parseInt(ConfigurationReader.GetNode("default_peer", "file_message_timeout_millis")));
                ObjectInputStream in = new ObjectInputStream(res.getInputStream());
                String message = (String) in.readObject();

                if (message.startsWith("voila")) {
                    String[] parts = message.split(" ");
                    String peerName = parts[1];
                    return this.peerContainer.getPeer(peerName);
                }

                res.close();
            } catch (IOException | ClassNotFoundException ignored) {
                // either the peer is not responding or the message is not what we expected, all those exceptions are handled by callers anyway.
            }

            return null;
        };

        return Executors.newCachedThreadPool().submit(task);
    }

    public long sendDownload(String fileName, int bounces) throws IOException, ExecutionException, InterruptedException, NoKnownPeersException {
        Peer peer = sendFindFile(fileName, bounces).get();

        if (peer == null) {
            throw new FileNotFoundException("Couldn't find the file [" + fileName + "] in any of the known peers.");
        }

        Socket socket = new Socket(peer.getAddress(), peer.getPort());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("download? %s", fileName));
        long size = in.readLong();

        byte[] data = new byte[2048];
        BufferedOutputStream bos = downloadsManager.getAsOutStream(fileName);
        int read, total;
        read = in.read(data, 0, data.length);
        bos.write(data, 0, read);
        total = read;

        while (read > -1 && total < size) {
            read = in.read(data, 0, data.length);
            if(read > -1)
            {
                bos.write(data, 0, read);
                total += read;
            }
        }

        bos.flush();
        bos.close();
        return size;
    }

    public String sendBye(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("bye! %s", this.name));
        return (String) in.readObject();
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
}
