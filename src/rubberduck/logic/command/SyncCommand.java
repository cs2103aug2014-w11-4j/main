package rubberduck.logic.command;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;
import rubberduck.menu.MenuInterface;
import rubberduck.storage.GooManager;

/**
 * Concrete Command Class that can be executed to perform a synchronization with
 * Google. The synchronization type is passed into the constructor to determine
 * what type of synchronization it should run.
 */
//@author A0111736M
public class SyncCommand extends Command {

    /**
     * The type of synchronization that is supported.
     */
    public enum SyncType {
        PUSH, PULL, FORCE_PUSH, FORCE_PULL, TWO_WAY, LOGOUT
    }

    private static final String MESSAGE_ERROR_FAIL_SAFE_CONNECTION =
        "Failed to initialize safe connection with server.";
    private static final String MESSAGE_ERROR_NETWORK_IOEXCEPTION =
        "Failed to connect to the server.";
    private static final String MESSAGE_UNDO_WARNING =
        "Note that once synchronization is done, you cannot undo previous actions.";
    private static final String MESSAGE_CONFIRM_PROMPT =
        "Are you sure you want to continue with the operation? (Y/N)";
    private static final String MESSAGE_SYNC_START =
        "Initiated syncing with Google.";
    private static final String MESSAGE_SYNC_CANCELLED =
        "Sync operation cancelled by user.";
    private static final String MESSAGE_PUSH_SUCCESS =
        "Successfully pushed data to Google.";
    private static final String MESSAGE_PULL_SUCCESS =
        "Successfully pulled data from Google.";
    private static final String MESSAGE_FORCEPUSH_SUCCESS =
        "Successfully pushed (forced) data to Google.";
    private static final String MESSAGE_FORCEPULL_SUCCESS =
        "Successfully pulled (forced) data from Google.";
    private static final String MESSAGE_TWOWAY_SUCCESS =
        "Successfully synchronise between RubberDuck and Google.";
    private static final String MESSAGE_LOGOUT_SUCCESS =
        "Successfully logged out from Google.";
    private static final String EXCEPTION_UNSUPPORTED_TYPE =
        "SyncType has not been implemented.";

    private SyncType type;

    /**
     * Public constructor of SyncCommand that accepts SyncType argument to
     * determine what type of synchronization to execute.
     *
     * @param type SyncType to represent what to sync
     */
    public SyncCommand(SyncType type) {
        this.type = type;
    }

    /**
     * Synchronize with Google based on the type user specified. Unless the user
     * specifies logout, a warning will be given first as undo/redo operation
     * can no longer be done and a confirmation will be prompted from the user.
     *
     * @return Response containing success or error message based on execution
     * @throws IOException occurs then GooManager encounters an I/O error
     */
    @Override
    public Response execute() throws IOException {
        if (type == SyncType.LOGOUT) {
            GooManager.logOut();
            return new Response(ColorFormatter.format(MESSAGE_LOGOUT_SUCCESS,
                                                      Color.GREEN), true);
        } else {
            String response = MenuInterface.getInstance().requestPrompt(
                ColorFormatter.format(MESSAGE_UNDO_WARNING, Color.YELLOW),
                ColorFormatter.format(MESSAGE_CONFIRM_PROMPT, Color.YELLOW));

            if (response.toLowerCase().contains("y")) {
                LOGGER.info(MESSAGE_SYNC_START);
                getDisplayedTasksList().clear();
                return startSync();
            } else {
                LOGGER.info(MESSAGE_SYNC_CANCELLED);
                return new Response(ColorFormatter.format(
                    MESSAGE_SYNC_CANCELLED, Color.RED), true);
            }
        }
    }

    /**
     * Synchronize with Google based on the SyncType the user specifies.
     *
     * @return Response object after synchronization
     * @throws IOException occurs then GooManager encounters an I/O error
     */
    private Response startSync() throws IOException {
        assert type != null : "Type must be initialized before syncing.";
        try {
            GooManager.initialize();
            switch (type) {
                case PUSH:
                    GooManager.pushAll(getDbManager());
                    return new Response(ColorFormatter.format(
                        MESSAGE_PUSH_SUCCESS, Color.GREEN), true);

                case PULL:
                    GooManager.pullAll(getDbManager());
                    return new Response(ColorFormatter.format(
                        MESSAGE_PULL_SUCCESS, Color.GREEN), true);

                case FORCE_PUSH:
                    GooManager.forcePushAll(getDbManager());
                    return new Response(ColorFormatter.format(
                        MESSAGE_FORCEPUSH_SUCCESS, Color.GREEN), true);

                case FORCE_PULL:
                    GooManager.forcePullAll(getDbManager());
                    return new Response(ColorFormatter.format(
                        MESSAGE_FORCEPULL_SUCCESS, Color.GREEN), true);

                case TWO_WAY:
                    GooManager.twoWaySync(getDbManager());
                    return new Response(ColorFormatter.format(
                        MESSAGE_TWOWAY_SUCCESS, Color.GREEN), true);

                default:
                    throw new UnsupportedOperationException(
                        EXCEPTION_UNSUPPORTED_TYPE);
            }
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_FAIL_SAFE_CONNECTION, e);
            return new Response(ColorFormatter.format(
                MESSAGE_ERROR_FAIL_SAFE_CONNECTION, Color.RED), true);
        } catch (GooManager.NetworkException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_NETWORK_IOEXCEPTION, e);
            return new Response(ColorFormatter.format(
                MESSAGE_ERROR_NETWORK_IOEXCEPTION, Color.RED), true);
        }
    }
}
