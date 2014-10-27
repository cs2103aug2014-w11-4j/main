package com.rubberduck.command;

import java.io.IOException;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.MenuInterface;

/**
 * Concrete Command Class that can be executed to clear the screen of the
 * terminal.
 *
 * @author hooitong
 */
public class ClearCommand extends Command {
    private static final String MESSAGE_CLEAR = "Screen cleared.";
    private static final String MESSAGE_IOEXCEPTION_ERROR = "Problem calling clearScreen in ConsoleReader.";

    /**
     * Clear the screen of the current terminal by calling the clearScreen
     * method in the current ConsoleReader instance. Will return error message
     * if encounter IOException when calling clearScreen.
     */
    @Override
    public String execute() {
        try {
            MenuInterface.getInstance().getConsoleInstance().clearScreen();
            return ColorFormatter.format(MESSAGE_CLEAR, Color.YELLOW);
        } catch (IOException e) {
            return MESSAGE_IOEXCEPTION_ERROR;
        }
    }
}
