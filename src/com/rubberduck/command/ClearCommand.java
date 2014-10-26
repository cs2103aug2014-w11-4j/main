package com.rubberduck.command;

import java.io.IOException;

import com.rubberduck.menu.MenuInterface;

public class ClearCommand extends Command {

    private static final String MESSAGE_CLEAR = "\u001B[33mScreen cleared.\u001B[0m";

    /**
     * Clear the screen of the current interface.
     *
     * @throws IOException
     */
    @Override
    public String execute() throws IOException {
        MenuInterface.getInstance().getConsoleInstance().clearScreen();
        return MESSAGE_CLEAR;
    }
}
