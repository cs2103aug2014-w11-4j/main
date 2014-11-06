package com.rubberduck.menu;

/**
 * Color formatter for ANSI-colored output for RubberDuck.
 */
//@author A0111736M
public class ColorFormatter {

    /**
     * Private constructor for ColorFormatter as it is a utility class.
     */
    private ColorFormatter() {

    }

    /**
     * Attribute that can be applied to text.
     */
    public enum Attribute {
        NORMAL(0), BRIGHT(1), DIM(2), UNDERLINE(4), BLINK(5), REVERSE(7),
        HIDDEN(8);

        private String stringValue;

        public String toString() {
            return "" + stringValue;
        }

        private Attribute(int stringValue) {
            this.stringValue = String.valueOf(stringValue);
        }
    }

    /**
     * Color that can be applied to foreground or background.
     */
    public enum Color {
        BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE
    }

    private static final String PREFIX = "\u001b[";
    private static final String SUFFIX = "m";
    private static final String END = PREFIX + SUFFIX;
    private static final String SEPARATOR = ";";
    private static final int FG_SUFFIX = 30;
    private static final int BG_SUFFIX = 40;

    /**
     * Format input string with given color as foreground.
     *
     * @param input input string
     * @param fg    foreground color
     * @return input that is color-coded
     */
    public static String format(String input, Color fg) {
        return format(input, null, fg, null);
    }

    /**
     * Format input string with given foreground color and background color.
     *
     * @param input input string
     * @param fg    foreground color
     * @param bg    background color
     * @return input that is color-coded
     */
    public static String format(String input, Color fg, Color bg) {
        return format(input, null, fg, bg);
    }

    /**
     * Format input string with given attribute, foreground color and background
     * color.
     *
     * @param input input string
     * @param atr   attribute
     * @param fg    foreground color
     * @param bg    background color
     * @return input that is color-coded
     */
    public static String format(String input, Attribute atr, Color fg,
                                Color bg) {
        StringBuilder sb = new StringBuilder();

        if (atr != null) {
            sb.append(atr);
        }

        if (fg != null) {
            if (sb.length() > 0) {
                sb.append(SEPARATOR);
            }
            sb.append(FG_SUFFIX + fg.ordinal());
        }

        if (bg != null) {
            if (sb.length() > 0) {
                sb.append(SEPARATOR);
            }
            sb.append(BG_SUFFIX + bg.ordinal());
        }

        sb.insert(0, PREFIX);
        sb.append(SUFFIX);
        sb.append(input);
        sb.append(END);
        return sb.toString();
    }
}
