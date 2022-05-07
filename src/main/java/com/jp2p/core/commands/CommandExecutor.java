package com.jp2p.core.commands;

import java.util.HashMap;

/**
 * The {@link CommandExecutor} stores a Map of {@link ICommand}s and their {@link CommandType} and executes an {@link ICommand} given its {@link CommandType}.
 */
public class CommandExecutor {
    /**
     * The map of {@link ICommand}s and their {@link CommandType} stored by the {@link CommandExecutor}.
     */
    private final HashMap<CommandType, ICommand> commandsDictionary;

    /**
     * Creates an empty {@link CommandExecutor}.
     */
    public CommandExecutor() {
        commandsDictionary = new HashMap<>();
    }

    /**
     * Adds an {@link ICommand} to the {@link CommandExecutor#commandsDictionary}.
     *
     * @param type    The {@link CommandType} of the {@link ICommand} to add.
     * @param command The reference to a class or a record that implements {@link ICommand}.
     */
    public void addCommand(CommandType type, ICommand command) {
        commandsDictionary.put(type, command);
    }

    /**
     * Executes the {@link ICommand} with the given {@link CommandType} passing it the given arguments.
     *
     * @param type The {@link CommandType} of the {@link ICommand} to execute.
     * @param args The arguments to pass to the {@link ICommand}.
     * @return The result of the {@link ICommand} execution.
     */
    public Object executeCommand(CommandType type, Object... args) {
        return commandsDictionary.get(type).execute(args);
    }
}
