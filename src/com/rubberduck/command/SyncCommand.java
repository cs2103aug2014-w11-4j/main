package com.rubberduck.command;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.rubberduck.io.GooManager;

public class SyncCommand extends Command {

    private static String MESSAGE_ERROR_UNABLE_TO_START_SAFE_CONNECTION = "Failed to initialize safe connection with server.";
    private static String MESSAGE_ERROR_NETWORK_IOEXCEPTION = "Failed to connect to the server.";

    @Override
    protected String execute() throws IOException {
        try {
            GooManager.initialize();
        } catch (GeneralSecurityException e) {
            return MESSAGE_ERROR_UNABLE_TO_START_SAFE_CONNECTION;
        } catch (IOException e) {
            return MESSAGE_ERROR_NETWORK_IOEXCEPTION;
        }

        return null;
    }
}
