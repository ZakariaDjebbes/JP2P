package com.jp2p.core.commands;

import com.jp2p.core.peer.Peer;
import com.jp2p.core.peer.PeerRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public record FileCommand(PeerRunner peerRunner) implements ICommand {
    @Override
    public Object execute(Object... args) {
        String fileName = (String) args[0];
        int bounces = Integer.parseInt((String) args[1]);

        if (bounces > 0) {
            for (File file : peerRunner.getFilesManager().getFiles()) {
                if (file.getName().equals(fileName)) {
                    return "voila";
                }
            }
            bounces--;

            try {
                Peer p = peerRunner.sendFindFile(fileName, bounces);

                if (p != null)
                    return "voila";

            } catch (FileNotFoundException e)
            {
                return "not found";
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            return "bounced";
        } else
            return "not found";
    }
}
