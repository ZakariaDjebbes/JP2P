package com.jp2p.core.commands;

import java.util.HashMap;

public class CommandExecutor {
    private final HashMap<CommandType, ICommand> commandsDictionary;

    public CommandExecutor() {
        commandsDictionary = new HashMap<>();
    }

    public void addCommand(CommandType type, ICommand command) {
        commandsDictionary.put(type, command);
    }

    public Object executeCommand(CommandType type, Object ...args) {
        return commandsDictionary.get(type).execute(args);
    }
}
