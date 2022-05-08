package com.jp2p.core.peer;

import com.jp2p.core.exceptions.NoKnownPeersException;
import com.jp2p.core.exceptions.PeerNotFoundException;
import com.jp2p.core.file.FileManager;
import com.jp2p.core.file.FolderManger;
import com.jp2p.core.file.PeerFile;
import com.jp2p.database.PeerConfigurationTable;

import java.io.*;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
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
    public static PeerRunner startUp() throws IOException, SQLException {
        PeerRunner peer;
        String defaultName = PeerConfigurationTable.getConfiguration("default_name");
        int maxPeers = Integer.parseInt(PeerConfigurationTable.getConfiguration("max_peers"));
        int defaultPort = Integer.parseInt(PeerConfigurationTable.getConfiguration("default_port"));

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

    /**
     * Starts the {@link PeerRunner} and waits for connections.
     */
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

    /**
     * Sends the name message to the {@link PeerRunner} at a given {@link Socket} and awaits to read its name.
     *
     * @param socket The {@link Socket} to send the message to.
     * @return The name of the {@link PeerRunner}.
     * @throws IOException            If an error occurs while writing the message on the {@link ObjectOutputStream}.
     * @throws ClassNotFoundException If an error occurs while reading the response from the {@link ObjectInputStream}.
     */
    public String sendGetName(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject("name?");
        return (String) in.readObject();
    }

    /**
     * Sends the known peers message to the {@link PeerRunner} at a given {@link Socket} and awaits to read the list of the known peers.
     *
     * @param socket The {@link Socket} to send the message to.
     * @return The list of known peers of the {@link PeerRunner}.
     * @throws IOException            If an error occurs while writing the message on the {@link ObjectOutputStream}.
     * @throws ClassNotFoundException If an error occurs while reading the response from the {@link ObjectInputStream}.
     */
    public String sendGetKnownPeers(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject("known peers?");
        return (String) in.readObject();
    }

    /**
     * Sends the it's me message to the {@link PeerRunner} at a given {@link Socket} and awaits to read the response.
     *
     * @param socket The {@link Socket} to send the message to.
     * @return The response from the {@link PeerRunner}. The result tells if the peer was added to the list of known peers or there was an error.
     * @throws IOException            If an error occurs while writing the message on the {@link ObjectOutputStream}.
     * @throws ClassNotFoundException If an error occurs while reading the response from the {@link ObjectInputStream}.
     */
    public String sendItsMe(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("it's me! %s %s %s", this.name, Inet4Address.getLocalHost().getHostAddress(), this.port));
        return (String) in.readObject();
    }

    /**
     * Sends the file message to the {@link PeerRunner} at a given {@link Socket}. Unlike other messages, this message is asynchronous and doesn't wait for a response
     * Responses to this message are handled by the voila message.
     *
     * @param fileName The search criteria to search for.
     * @param bounces  The number of bounces to the known peers of known peers.
     * @throws IOException           If an error occurs while writing the message on the {@link ObjectOutputStream}.
     * @throws NoKnownPeersException If no known peers are available.
     */
    public void sendFindFile(String fileName, int bounces) throws IOException, NoKnownPeersException {
        if (this.peerContainer.getPeers().size() == 0)
            throw new NoKnownPeersException("No known peers to search for a file.");

        for (Peer p : this.peerContainer.getPeers()) {
            Socket socket = new Socket(p.getAddress(), p.getPort());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(String.format("file? %s %s %s %s", fileName, bounces, this.server.getLocalPort(), Inet4Address.getLocalHost().getHostAddress()));
        }
    }

    /**
     * Sends the download message to the {@link PeerRunner} at a given {@link Socket}.
     * Reads the total number of bytes to be downloaded, then reads and saves them on the {@link PeerRunner#downloadsFolderManager}.
     * If a previous download of that file failed, the download will proceed from the last failed byte and continue on.
     * Otherwise, the download will start from the beginning.
     * This is done by sending the last successfully downloaded byte to the {@link PeerRunner}, the {@link PeerRunner} will skip the first byte and starts sending bytes from that point.
     * This ensures that if the connection is lost at some point, the download will resume instead of start over.
     * The message is in this format: download? [file name] [skip bytes]
     *
     * @param index The index of the file to download in the {@link PeerRunner#filesFoundManager}.
     * @return true if the file was completely downloaded, false otherwise.
     * @throws IOException           If an error occurs while writing the message on the {@link ObjectOutputStream}.
     * @throws PeerNotFoundException If the peer is not found.
     */
    public boolean sendDownload(int index) throws IOException, PeerNotFoundException {
        PeerFile peerFile = filesFoundManager.getPeerNameAt(index);
        Peer peer = peerContainer.getPeer(peerFile.getPeerName());

        if (peer == null)
            throw new PeerNotFoundException();

        if (peerFile.getDownloadedSize() == peerFile.getFileSize())
            // If the downloaded size is equal to the file size, it means that the file was correctly downloaded previously and w are downloading it again so reset it to 0.
            peerFile.setDownloadedSize(0);

        Socket socket = new Socket(peer.getAddress(), peer.getPort());
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("download? %s %s", peerFile.getFileName(), peerFile.getDownloadedSize()));
        long size = in.readLong();

        byte[] data = new byte[2048];
        // if we previously failed to download the file, we will resume from the last successfully downloaded byte
        // meaning we will append the data to the file instead of overwriting it
        BufferedOutputStream bos = downloadsFolderManager.getAsOutStream(peerFile.getFileName(), !(peerFile.getDownloadedSize() == 0));
        int read;
        int total;

        read = in.read(data, 0, data.length);
        total = read;
        peerFile.setDownloadedSize(peerFile.getDownloadedSize() + read);
        bos.write(data, 0, read);


        while (read > -1 && total < size) {
            read = in.read(data, 0, data.length);
            if (read > -1) {
                total += read;
                peerFile.setDownloadedSize(peerFile.getDownloadedSize() + read);
                bos.write(data, 0, read);
            }
        }

        bos.flush();
        bos.close();

        if (peerFile.getDownloadedSize() == size) {
            // if the total downloaded size is equal to the file size, it means that the file was correctly downloaded
            peerFile.setWasDownloaded(true);
        }

        return peerFile.getWasDownloaded();
    }

    /**
     * Sends the voila message to the {@link PeerRunner} at a given {@link Socket}. The message is sent whenever the file message finds at least one file that matches the search criteria.
     * The message is always in this format: "voila! [peer name] [number of files found] [file name 1] [file size1] [file name 2] [file size2]...".
     *
     * @param socket The {@link Socket} to send the message to.
     * @param files  The list of files that were found through the file message.
     * @throws IOException If an error occurs while writing the message on the {@link ObjectOutputStream}.
     */
    public void sendVoila(Socket socket, ArrayList<File> files) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        StringBuilder builder = new StringBuilder();
        builder.append("voila! ").append(name).append(" ").append(files.size());

        for (File file : files) {
            builder.append(" ").append(file.getName()).append(" ").append(file.length());
        }

        out.writeObject(builder.toString());
        out.flush();
    }

    /**
     * Sends the bye message to the {@link PeerRunner} at a given {@link Socket}. Awaits the response from the peer.
     *
     * @param socket The {@link Socket} to send the message to.
     * @return The response from the peer.
     * @throws IOException            If an error occurs while writing the message on the {@link ObjectOutputStream}.
     * @throws ClassNotFoundException If an error occurs while reading the message from the {@link ObjectInputStream}.
     */
    public String sendBye(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(String.format("bye! %s", this.name));
        return (String) in.readObject();
    }

    /**
     * Returns the name of the peer.
     * @return the name of the peer.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link PeerRunner#peerContainer} of the peer.
     * @return the {@link PeerContainer} that is used to store the list of known peers of this peer.
     */
    public PeerContainer getPeerContainer() {
        return peerContainer;
    }

    /**
     * Returns the {@link PeerRunner#filesFolderManager} of the peer.
     * @return the {@link FolderManger} that is used to store the list of files that are shared by this peer.
     */
    public FolderManger getFilesFolderManager() {
        return filesFolderManager;
    }

    /**
     * Returns the {@link PeerRunner#filesFoundManager} of the peer.
     * @return the {@link FileManager} that is used to store the list of files discovered by this peer.
     */
    public FileManager getFilesFoundManager() {
        return filesFoundManager;
    }
}
