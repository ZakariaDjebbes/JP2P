package com.jp2p.core.file;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class is used to manage the folders of the peer.
 * It's used to find files in a folder and to create {@link java.util.stream.Stream}s to write and read files.
 * Manages the folder of the files shared by the peer of the network and the folder of the files that have been downloaded by the peer.
 */
public class FolderManger {

    /**
     * The path to the folder where the files are stored.
     */
    private final String path;

    /**
     * The {@link File}s in the folder.
     */
    private File[] files;

    /**
     * Constructs a new {@link FolderManger} with the given path to a folder. And looks up the {@link File}s in the folder.
     *
     * @param path The path to the folder.
     */
    public FolderManger(String path) {
        this.path = path;
        lookupFiles();
    }

    /**
     * Looks up the {@link File}s in the folder and adds them to the list of {@link FolderManger#files}.
     * This method is synchronized to avoid concurrent access to the list of {@link FolderManger#files}.
     */
    public synchronized void lookupFiles() {
        File curDir = new File(path);

        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        files = curDir.listFiles();
    }

    /**
     * Returns a {@link BufferedInputStream} of a given file to read in the folder targeted by the {@link FolderManger#path}.
     *
     * @param fileName The name of the file to read.
     * @return A {@link BufferedInputStream} of the file to read.
     * @throws FileNotFoundException If the file is not found in the folder.
     */
    public BufferedInputStream getAsInStream(String fileName) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(path + fileName));
    }

    /**
     * Returns a {@link BufferedOutputStream} of a given file to write in the folder targeted by the {@link FolderManger#path}.
     *
     * @param fileName The name of the file to write.
     * @return A {@link BufferedOutputStream} of the file to write.
     * @throws FileNotFoundException If the file is not found in the folder.
     */
    public BufferedOutputStream getAsOutStream(String fileName) throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(path + fileName));
    }

    /**
     * Returns the list of {@link File}s in the folder that match a search criteria.
     * This method is synchronized to avoid concurrent access to the list of {@link FolderManger#files}.
     *
     * @param match The search criteria.
     * @return The list of {@link File}s in the folder that match the search criteria.
     */
    public synchronized ArrayList<File> getFiles(String match) {
        ArrayList<File> res = new ArrayList<>();

        for (File file : files) {
            if (file.getName().contains(match)) {
                res.add(file);
            }
        }

        return res;
    }

    /**
     * Returns the {@link File}s in the folder that match the exact same name passed.
     * This method is synchronized to avoid concurrent access to the list of {@link FolderManger#files}.
     *
     * @param fileName The name of the file to search.
     * @return The {@link File}s in the folder that match the exact same name passed.
     * @throws FileNotFoundException If the file is not found in the folder.
     */
    public synchronized File getFile(String fileName) throws FileNotFoundException {
        for (File f : files) {
            if (f.getName().equals(fileName)) {
                return f;
            }
        }

        throw new FileNotFoundException("File not found: " + fileName);
    }
}
