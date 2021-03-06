package rubberduck.common.datatransfer;

import rubberduck.common.formatter.Formatter;

//@author A0111736M
/**
 * A data structure that must be created by each command and returned back to
 * the MenuInterface for formatting into the buffer accordingly.
 */
public class Response {

    private String[] messages;
    private String viewCount;
    private String viewData;
    private boolean isOverwrite;

    /**
     * Public constructor for Response which accepts all three input, messages,
     * view count and the view data.
     *
     * @param messages  message displayed at the top portion
     * @param viewCount data which explains the view table
     * @param viewData  actual task data of current view format
     */
    public Response(String messages, String viewCount, String viewData) {
        setMessages(messages);
        this.viewCount = viewCount;
        this.viewData = viewData;
    }

    /**
     * Public constructor for Response which accepts only messages. Buffer will
     * not be split into three portion but only display the messages instead.
     */
    public Response(String messages, boolean isOverwrite) {
        setMessages(messages);
        this.isOverwrite = isOverwrite;
    }

    /**
     * Public getter method for messages.
     *
     * @return messages as String array
     */
    public String[] getMessages() {
        return messages;
    }

    /**
     * Public setter method for messages.
     *
     * @param messages to overwrite
     */
    public void setMessages(String messages) {
        this.messages = Formatter.formatMessage(messages);
    }

    /**
     * Public getter method for viewCount.
     *
     * @return viewCount as String
     */
    public String getViewCount() {
        return viewCount;
    }

    /**
     * Public getter method for viewData.
     *
     * @return viewData as String
     */
    public String getViewData() {
        return viewData;
    }

    /**
     * Public getter method for isOverwrite. If true, menu should overwrite
     * buffer, else retain old task buffer.
     *
     * @return if the value isOverwrite
     */
    public boolean isOverwrite() {
        return isOverwrite;
    }
}
