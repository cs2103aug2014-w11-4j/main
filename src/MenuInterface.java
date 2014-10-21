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

    private static MenuInterface menuInstance;

    /**
     * Private Constructor for Singleton Implementation.
     */
    private MenuInterface() {
    }

    /**
     * Method that retrieves the singleton instance of the MenuInterface
     *
     * @return instance of Parser
     */
    public static MenuInterface getInstance() {
        if (menuInstance == null) {
            menuInstance = new MenuInterface();
        }

        return menuInstance;
    }

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
            Command userCommand = Parser.getInstance().parse(rawInput);
            String response = Logic.getInstance().executeCommand(userCommand);
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

    /**
     * Method that outputs a string object to the CLI.
     *
     * @param s String object
     */
    private void showToUser(String s) {
        System.out.println(s);
    }
}
