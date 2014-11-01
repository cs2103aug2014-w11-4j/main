package com.rubberduck.command;

import com.rubberduck.io.GooManager;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.MenuInterface;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Concrete Command Class that can be executed to synchronise with Google based on the type.
 *
 * @author hooitong
 */
public class SyncCommand extends Command {

    private static final String
        MESSAGE_ERROR_UNABLE_TO_START_SAFE_CONNECTION =
        "Failed to initialize safe connection with server.";
    private static final String
        MESSAGE_ERROR_NETWORK_IOEXCEPTION =
        "Failed to connect to the server.";
    private static final String
        MESSAGE_UNDO_WARNING =
        "Note that once synchronization is done, you cannot undo your previous actions.";
    private static final String
        MESSAGE_CONFIRM_PROMPT =
        "Are you sure you want to continue with the operation? (Y/N)";
    private static final String MESSAGE_SYNC_CANCELLED = "Sync operation cancelled by user.";
    private static final String MESSAGE_PUSH_SUCCESS = "Successfully pushed data to Google.";
    private static final String MESSAGE_PULL_SUCCESS = "Successfully pulled data from Google.";
    private static final String
        MESSAGE_FORCEPUSH_SUCCESS =
        "Successfully pushed (forced) data to Google.";
    private static final String
        MESSAGE_FORCEPULL_SUCCESS =
        "Successfully pulled (forced) data from Google.";
    private static final String
        MESSAGE_TWOWAY_SUCCESS =
        "Successfully synchronise between RubberDuck and Google.";

    public enum SyncType {
        PUSH, PULL, FORCE_PUSH, FORCE_PULL, TWO_WAY
    }

    private SyncType type;

    /**
     * Public constructor of SyncCommand that accepts a enum to determine what type of
     * synchronization to execute.
     *
     * @param type SyncType to represent what to sync
     */
    public SyncCommand(SyncType type) {
        assert type != null : "Should never be constructed with null";
        this.type = type;
    }

    /**
     * Synchronize with Google based on the type user specified.
     *
     * @return success or error message based on execution result
     */
    @Override
    public String execute() throws IOException {
        try {
            String response = MenuInterface.getInstance()
                .requestPrompt(
                    ColorFormatter.format(MESSAGE_UNDO_WARNING,
                                          Color.YELLOW),
                    ColorFormatter.format(MESSAGE_CONFIRM_PROMPT,
                                          Color.YELLOW));

            if (response.contains("y")) {
                GooManager.initialize();
                switch (type) {
                    case PUSH:
                        GooManager.pushAll(getDbManager());
                        return ColorFormatter.format(MESSAGE_PUSH_SUCCESS,
                                                     Color.GREEN);

                    case PULL:
                        GooManager.pullAll(getDbManager());
                        return ColorFormatter.format(MESSAGE_PULL_SUCCESS,
                                                     Color.GREEN);

                    case FORCE_PUSH:
                        GooManager.forcePushAll(getDbManager());
                        return ColorFormatter.format(MESSAGE_FORCEPUSH_SUCCESS,
                                                     Color.GREEN);

                    case FORCE_PULL:
                        GooManager.forcePullAll(getDbManager());
                        return ColorFormatter.format(MESSAGE_FORCEPULL_SUCCESS,
                                                     Color.GREEN);

                    case TWO_WAY:
                        GooManager.twoWaySync(getDbManager());
                        return ColorFormatter.format(MESSAGE_TWOWAY_SUCCESS,
                                                     Color.GREEN);

                    default:
                        throw new UnsupportedOperationException(
                            "Type has not been implemented.");
                }
            } else {
                return ColorFormatter.format(MESSAGE_SYNC_CANCELLED, Color.RED);
            }
        } catch (GeneralSecurityException e) {
            return ColorFormatter.format(
                MESSAGE_ERROR_UNABLE_TO_START_SAFE_CONNECTION, Color.RED);
        } catch (IOException e) {
            return ColorFormatter.format(MESSAGE_ERROR_NETWORK_IOEXCEPTION,
                                         Color.RED);
        }
    }
}
