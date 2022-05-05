package com.jp2p.core.commands;

@FunctionalInterface
public interface ICommand {
    Object execute(Object ...args);
}
