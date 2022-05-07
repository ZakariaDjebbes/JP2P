package com.jp2p.core.commands;

/**
 * Represents the basic functionality of a command. This is a Functional Interface meaning it has only one method.
 * This interface must be implemented by all commands.
 */
@FunctionalInterface
public interface ICommand {
    /**
     * Executes the given definition of a command defined by the class or record that implements the {@link ICommand} interface.
     *
     * @param args The arguments used by the command. There is no limit to the number of arguments providing they are {@link Object}s.
     * @return The result of the command.
     */
    Object execute(Object... args);
}
