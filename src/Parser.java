/**
 * Parser that reads in raw user input and provides instruction on how the UI
 * should call for the correction execution at the logic.
 *
 * @author hooitong
 *
 */
public class Parser {
    public static Command parse(String input) {
        CommandType userCommand = determineCommandType(input);
        return parseCommand(userCommand);
    }

    private static CommandType determineCommandType(String input) {
        String command = getFirstWord(input);
        return CommandType.getCommandType(command);
    }

    private static Command parseCommand(CommandType userCommand)
            throws UnsupportedOperationException {
        switch (userCommand) {
            case VIEW:
                throw new UnsupportedOperationException(
                        "Command not integrated yet");

            case SEARCH:
                throw new UnsupportedOperationException(
                        "Command not integrated yet");

            case ADD:
                throw new UnsupportedOperationException(
                        "Command not integrated yet");

            case DELETE:
                throw new UnsupportedOperationException(
                        "Command not integrated yet");

            case UPDATE:
                throw new UnsupportedOperationException(
                        "Command not integrated yet");

            case EXIT:
                throw new UnsupportedOperationException(
                        "Command not integrated yet");

            case INVALID:
                return new Command(userCommand);

            default:
                throw new UnsupportedOperationException(
                        "Command not integrated yet");
        }

    }

    /* Helper Methods for Parser */
    private static String getFirstWord(String input) {
        return input.split("\\s+", 2)[0];
    }

    private static String removeFirstWord(String input) {
        String[] splitWord = input.split("\\s+", 2);
        return splitWord.length == 1 ? splitWord[0] : splitWord[1];
    }
}
