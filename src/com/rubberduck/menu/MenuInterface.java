package com.rubberduck.menu;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import com.rubberduck.command.Command;
import com.rubberduck.logic.Parser;

/**
 * This class focuses on handling the user interface of the entire application
 * which accepts the user's input, call the parser and calls the correct method
 * in the logic.
 *
 * @author hooitong
 *
 */
public class MenuInterface {
    private static final String MESSAGE_WELCOME = "Welcome to RubberDuck. Here's your agenda for today.";
    private static final String MESSAGE_HELP = "If you need a list of commands, type ? or help.";
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
     * @throws Exception 
     */
    public void handleInterface() throws Exception {
        //author: JasonSia
        ConsoleReader cr = new ConsoleReader();
        cr.setPrompt(">");
        cr.isPaginationEnabled();
        List<Completer> completors = new LinkedList<Completer>();
        completors.add(new StringsCompleter("view", "display", "find",
                "lookup", "search", "add", "insert", "ins", "new", "delete",
                "remove", "change", "update", "edit", "undo", "ud", "redo",
                "rd", "mark", "completed", "done", "confirm", "mark",
                "completed", "done", "help", "cls", "clear", "exit", "quit"));

        for (Completer c : completors) {
            cr.addCompleter(c);
        }
        PrintWriter out = new PrintWriter(cr.getOutput());
        showWelcome(out);
        while (true) {
            String line = cr.readLine(">");
            Command userCommand = Parser.getInstance().parse(line);
            String response = userCommand.safeExecute();
            
            showToUser(response,out);

        }
    }

    /**
     * Method that is used to show the welcome screen and information when user
     * first run.
     */
    private void showWelcome(PrintWriter out) {
        showToUser(MESSAGE_WELCOME,out);
        Command userCommand = Parser.getInstance().parse("view today");
        String response = userCommand.safeExecute();
        showToUser(response,out);
        showToUser(MESSAGE_HELP,out);
    }


    /**
     * Method that outputs a string object to the CLI.
     *
     * 
     * @param s String object
     * @param out PrintWriter object
     */
    private void showToUser(String s,PrintWriter out) {
        out.println(s);                 
    }
}
