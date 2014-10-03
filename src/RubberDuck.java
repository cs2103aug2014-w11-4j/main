/**
 * RubberDuck is a CLI Task Manager that stores user's task into a text file and
 * helps the user to handle all his/her tasks via this application.
 *
 * This class is used to start and initialize the program.
 *
 * @author hooitong
 *
 */
public class RubberDuck {
    public static void main(String[] args) {
        MenuInterface menu = new MenuInterface();
        Logic.startDatabase();
        Parser.initParser();
        menu.handleInterface();
    }
}
