package com.jp2p.core.commands;

import com.jp2p.core.exceptions.NoKnownPeersException;
import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public record FileCommand(PeerRunner peerRunner) implements ICommand {
    @Override
    public Object execute(Object... args) {
        String fileName = (String) args[0];
        int bounces = Integer.parseInt((String) args[1]);
        int outputPort = Integer.parseInt((String) args[2]);
        String outputAddress = (String) args[3];

        try {
            ObjectOutputStream out = new ObjectOutputStream(new Socket(outputAddress, outputPort).getOutputStream());

            if (bounces > 0) {
                for (File file : peerRunner.getFilesManager().getFiles()) {
                    if (file.getName().equals(fileName)) {
                        out.writeObject("voila " + peerRunner.getName());
                        out.flush();
                        return "done";
                    }
                }

                bounces--;
                peerRunner.sendFindFile(fileName, bounces);
            } else {
                out.writeObject("not found");
                out.flush();
                return "failed";
            }
        } catch (Exception | NoKnownPeersException ignored) {
            // ignored, we don't care because it will be handled by the caller
        }

        return "done";
    }
}
