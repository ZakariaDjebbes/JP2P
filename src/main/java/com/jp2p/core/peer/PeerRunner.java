package com.jp2p.core.peer;

import com.jp2p.configuration.ConfigurationReader;
import com.jp2p.core.exceptions.NoKnownPeersException;
import com.jp2p.core.exceptions.PeerNotFoundException;
import com.jp2p.core.file.FileManager;
import com.jp2p.core.file.FolderManger;
import com.jp2p.core.file.PeerFile;

import java.io.*;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is responsible for the peer's main loop.
 * Defines the state of peer in the system, its folders and acts as both a Client and a Server.
 * Implements the {@link Runnable} interface.
 */
@SuppressWarnings("InfiniteLoopStatement")
public class PeerRunner implements Runnable {
    /**
     * The path to the peer's folder that contains the files shared in the network.
     */
    public static final String PEER_FILE_PATH = "./files/";

    /**
     * The path to the peer's folder in which files will be downloaded.
     */
    public static final String PEER_DOWNLOADS_PATH = "./downloads/";

    /**
     * The maximum number of threads that can act as slaves for the Server.
     */
    public static final int MAX_THREADS = 10;

    /**
     * The port that the peer will be listening on.
     */
    private final int port;

    /**
     * The {@link ServerSocket} that will be used to listen for connections.
     */
    private final ServerSocket server;

    /**
     * The {@link ExecutorService} that will be used to execute the slaves.
     */
    private final ExecutorService slavePool;

    /**
     * The name of the peer.
     */
    private final String name;

    /**
     * The {@link PeerContainer} that will be used to store and interact with the list of known peers of this peer.
     */
    private final PeerContainer peerContainer;

    /**
     * The {@link FolderManger} that will be used to manage the folder that contains the files shared by the peer on the network.
     */
    private final FolderManger filesFolderManager;

    /**
     * The {@link FolderManger} that will be used to manage the folder that contains the files downloaded by the peer.
     */
    private final FolderManger downloadsFolderManager;

    /**
     * The {@link FileManager} that will be used to manage the files discovered by the peer using the file message on the network.
     */
    private final FileManager filesFoundManager;

    /**
     * Constructs a new {@link PeerRunner} with the given parameters.
     * This is a private constructor because the first peer to ever be created will have default values for the parameters (Port and IP Address).
     * The subsequent peers will be created with the values given by the user.
     * To get a new instance of {@link PeerRunner} use the {@link PeerRunner#startUp()} method which follows the flow described above.
     *
     * @param name     The name of the peer.
     * @param port     The port that the peer will be listening on.
     * @param maxPeers The maximum number of peers that the peer will be able to know.
     * @throws IOException If an error occurs while creating the {@link ServerSocket}.
     */
    private PeerRunner(String name, int port, int maxPeers) throws IOException {
        this.name = name;
        this.port = port;
        this.peerContainer = new PeerContainer(maxPeers);
        this.filesFolderManager = new FolderManger(PEER_FILE_PATH);
        this.downloadsFolderManager = new FolderManger(PEER_DOWNLOADS_PATH);
        this.filesFoundManager = new FileManager();
        this.server = new ServerSocket(port);
        this.slavePool = Executors.newFixedThreadPool(MAX_THREADS);
    }

    /**
     * Create a new {@link PeerRunner} with the default values by the configuration, if a {@link PeerRunner} is already running on the default configuration then asks the user for the parameters.
     *
     * @return The new {@link PeerRunner} with the default values or the values entered by the user.
     * @throws IOException If an error occurs while creating the {@link ServerSocket}.
     */
    public static PeerRunner startUp() throws IOException {
        PeerRunner peer;
        String defaultName = ConfigurationReader.GetNode("default_peer", "name");
        int maxPeers = Integer.parseInt(ConfigurationReader.GetNode("default_peer", "max_peers"));
        int defaultPort = Integer.parseInt(ConfigurationReader.GetNode("default_peer", "port"));

        try {
            peer = new PeerRunner(defaultName, defaultPort, maxPeers);
            System.out.println("Because this is the first peer, It will always run on port " + defaultPort + " Go to the configuration file to change the default port.");
        } catch (BindException e) {
            System.out.print("Choose a name for this peer (Must be unique across the network!): ");
            String name = new Scanner(System.in).nextLine();
            System.out.print("Choose an open port for this peer: ");
            int port = new Scanner(System.in).nextInt();
            peer = new PeerRunner(name, port, maxPeers);
        }

        return peer;
    }

    @Override
    public void run() {
        System.out.printf("Peer [%s] up and listening for other peers on port [%s]...%n", name, port);
        try {
            while (true) {
                this.slavePool.execute(new PeerTask(server.accept(), this));
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

    public void sendFindFile(String fileName, int bounces) throws IOException, NoKnownPeersException {
        if (this.peerContainer.getPeers().size() == 0)
            throw new NoKnownPeersException("No known peers to search for file.");

        for (Peer p : this.peerContainer.getPeers()) {
            Socket socket = new Socket(p.getAddress(), p.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(String.format("file? %s %s %s %s", fileName, bounces, this.server.getLocalPort(), Inet4Address.getLocalHost().getHostAddress()));
        }
    }

    public long sendDownload(int index) throws IOException, PeerNotFoundException {
        PeerFile peerFile = filesFoundManager.getPeerNameAt(index);
        Peer peer = peerContainer.getPeer(peerFile.getPeerName());

        if (peer == null)
            throw new PeerNotFoundException();

        Socket socket = new Socket(peer.getAddress(), peer.getPort());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("download? %s", peerFile.getFileName()));
        long size = in.readLong();

        byte[] data = new byte[2048];
        BufferedOutputStream bos = downloadsFolderManager.getAsOutStream(peerFile.getFileName());
        int read, total;
        read = in.read(data, 0, data.length);
        bos.write(data, 0, read);
        total = read;

        while (read > -1 && total < size) {
            read = in.read(data, 0, data.length);
            if (read > -1) {
                bos.write(data, 0, read);
                total += read;
            }
        }

        bos.flush();
        bos.close();
        peerFile.setDownloaded(true);
        return size;
    }

    public void sendVoila(Socket socket, ArrayList<File> files) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        StringBuilder builder = new StringBuilder();
        builder.append("voila! ").append(name).append(" ").append(files.size());

        for (File file : files) {
            builder.append(" ").append(file.getName());
        }

        out.writeObject(builder.toString());
        out.flush();
    }

    public String sendBye(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("bye! %s", this.name));
        return (String) in.readObject();
    }

    /**
     * @return the name of the peer.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the {@link PeerContainer} that is used to store the list of known peers of this peer.
     */
    public PeerContainer getPeerContainer() {
        return peerContainer;
    }

    /**
     * @return the {@link FolderManger} that is used to store the list of files that are shared by this peer.
     */
    public FolderManger getFilesFolderManager() {
        return filesFolderManager;
    }

    /**
     * @return the {@link FileManager} that is used to store the list of files discovered by this peer.
     */
    public FileManager getFilesFoundManager() {
        return filesFoundManager;
    }
}
