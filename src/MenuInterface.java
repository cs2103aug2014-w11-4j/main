import java.util.Scanner;

/**
 * This class focuses on handling the user interface of the entire application
 * which accepts the user's input, call the parser and calls the correct method
 * in the logic.
 *
 * @author hooitong
 *
 */
public class MenuInterface {
    private static final String MESSAGE_WELCOME = "Welcome to RubberDuck.";

    /**
     * Method that handles the interface of the program. It prompts from user
     * and calls the parser to determine the command to be executed. It then
     * proceed to execute the given command if it is valid.
     */
    public void handleInterface() {
        Scanner sc = new Scanner(System.in);
        showToUser(MESSAGE_WELCOME);
        while (true) {
            String rawInput = acceptInput(sc);
            Command userCommand = Parser.parse(rawInput);
            String response = Logic.executeCommand(userCommand);
            showToUser(response);
        }
    }

    /**
     * Accept raw input from user via CLI and return to parent.
     *
     * @return raw user's input
     */
    private String acceptInput(Scanner sc) {
        System.out.print("> ");
        return sc.nextLine();
    }

    /* Helper Methods for Parser */
    private void showToUser(String s) {
        System.out.println(s);
    }
}
