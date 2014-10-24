import java.io.IOException;
import java.util.ArrayList;


public class AddCommand extends Command{
	private static final String MESSAGE_ADD = "\"%s\" has been successfully added.";
	private static final String MESSAGE_ADD_CONFLICT = "\"%s\" has been successfully added.\nPlease note that there are conflicting task(s).";
	private static final String MESSAGE_ADD_PAST = "\"%s\" cannot be added as the end date has already passed.";
	private static final String JOURNAL_MESSAGE_ADD = "Added task \"%s\"";
	   
    public AddCommand(CommandType type, String desc, ArrayList<DatePair> datePairs) {
        this.type = type;
        this.description = desc;
        this.datePairs = datePairs;
    }

	@Override
	public String execute() throws IOException {
		assert datePairs != null;
		assert description != null;
		assert !description.equals("");

		if (isDateBeforeNow(datePairs)) {
			return String.format(MESSAGE_ADD_PAST, description);
	    }

	    Task task = new Task(description, datePairs);

	    assert task != null;
	    boolean hasConflict = checkConflictWithDB(task);
	    long id = dbManager.putInstance(task);
	    dbManager.recordAction(null, id,	
	    		String.format(JOURNAL_MESSAGE_ADD, task.getDescription()));
	    assert id >= 0;

	    if (hasConflict) {
	    	return String.format(MESSAGE_ADD_CONFLICT, description);
	    } else {
	    	return String.format(MESSAGE_ADD, description);
	     }
	}
	
}
