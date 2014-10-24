//@author A0119416H

import com.thoughtworks.xstream.XStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A synchronized database backend to read/write instances to a file using XML
 * <p/>
 * All instances are stored on the disk and fetched on spot.
 * <p/>
 * A written instance cannot be modified, modifying must be done by removing the
 * old instance (by marking as invalid) and insert the new one.
 *
 * @param <T> The data type, which has to be a Java Bean class.
 */
public class DatabaseManager<T extends Serializable & Comparable<T>> implements Iterable<T> {

    private class InstanceIdComparator implements Comparator<Long> {
        @Override
        public int compare(Long o1, Long o2) {
            try {
                return getInstance(o1).compareTo(getInstance(o2));
            } catch (IOException e) {
                throw new UnsupportedOperationException("IOException: "
                        + e.getMessage());
            }
        }
    }

    public Comparator<Long> getInstanceIdComparator() {
        return new InstanceIdComparator();
    }

    private class InstanceIterator implements Iterator<T> {
        private Iterator<Long> offsetIterator = validInstancesMap.keySet()
                .iterator();

        @Override
        public boolean hasNext() {
            return offsetIterator.hasNext();
        }

        @Override
        public T next() {
            try {
                return getInstance(offsetIterator.next());
            } catch (IOException e) {
                throw new UnsupportedOperationException("IOException: "
                        + e.getMessage());
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove instance.");
        }
    }

    /**
     * Returns an iterator over valid instances in the database. Note that
     * IOException happened while reading instances will be thrown as
     * UnsupportedOperationException.
     *
     * @return an Iterator over valid instances in the database.
     */
    @Override
    public Iterator<T> iterator() {
        return new InstanceIterator();
    }

    /**
     * Flags to be used to mark the entity.
     */
    private static String VALID_FLAG = "#DBMNGR_VALID";
    private static String INVALID_FLAG = "#DBMNGR_INVAL";

    /**
     * eofOffset records the end of the file, which is needed to insert new
     * instances.
     */
    private RandomAccessFile randomAccessFile = null;
    private String filePath;
    private long eofOffset;

    /**
     * Store the IDs of both valid and invalid instances and their offset.
     */
    private HashMap<Long, Long> validInstancesMap = null;
    private HashMap<Long, Long> invalidInstancesMap = null;
    private long currentId;

    private JournalController<T> journal;

    private static XStream xstream = new XStream();

    /**
     * Construct a backend database with the given file path.
     *
     * @param filePath path to the database file. If exists it must be readable
     *            and writable.
     * @throws FileNotFoundException if the file cannot be opened (non-writable)
     * @throws IOException
     */
    public DatabaseManager(String filePath) throws IOException {
        this.filePath = filePath;
        openFile();
        scanFile();
    }

    private long createNewId() {
        currentId++;
        return currentId;
    }

    private void resetId() {
        currentId = 0;
    }

    private void resetJournal() {
        journal = new JournalController<T>(this);
    }

    /**
     * Attempt to open the file for r/w
     *
     * @throws FileNotFoundException if the file cannot be opened (non-writable)
     */
    private void openFile() throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(new File(filePath), "rws");
    }

    /**
     * Attempt to scan the file to get the offsets for existing valid instances.
     *
     * @throws IOException
     */
    private void scanFile() throws IOException {
        resetId();
        resetJournal();
        validInstancesMap = new HashMap<Long, Long>();
        invalidInstancesMap = new HashMap<Long, Long>();
        randomAccessFile.seek(0);
        long offset = randomAccessFile.getFilePointer();
        String line;
        while ((line = randomAccessFile.readLine()) != null) {
            if (line.equals(VALID_FLAG)) {
                validInstancesMap.put(createNewId(), offset);
            }
            offset = randomAccessFile.getFilePointer();
        }
        eofOffset = offset;
    }

    /**
     * Write the changes and close the file.
     *
     * @throws IOException
     */
    public void closeFile() throws IOException {
        writeChangesAndClose();
    }

    /**
     * Write all the changes and remove invalid instances. Then reopen the file.
     * Note that IDs of instances will change after the operation.
     *
     * @throws IOException
     */
    public void rewriteFile() throws IOException {
        writeChangesAndClose();
        openFile();
        scanFile();
    }

    /**
     * Delete all instances and reset the database.
     *
     * @throws IOException
     */
    public void resetDatabase() throws IOException {
        randomAccessFile.setLength(0);
        rewriteFile();
    }

    /**
     * Rewrite the file with all valid instances and close the file. All invalid
     * instances are discarded.
     *
     * @throws IOException
     */
    private void writeChangesAndClose() throws IOException {
        File tempFile = File.createTempFile("DBMNGR", ".tmp");
        tempFile.deleteOnExit();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
                tempFile));
        randomAccessFile.seek(0);
        String line;
        boolean willCopy = false;
        while ((line = randomAccessFile.readLine()) != null) {
            if (line.equals(VALID_FLAG)) {
                willCopy = true;
            } else if (line.equals(INVALID_FLAG)) {
                willCopy = false;
            }
            if (willCopy) {
                bufferedWriter.write(line);
                bufferedWriter.write('\n');
            }
        }
        bufferedWriter.close();
        randomAccessFile.close();
        tempFile.renameTo(new File(filePath));
    }

    private String getStringAtOffset(long offset) throws IOException {
        randomAccessFile.seek(offset);
        String line = randomAccessFile.readLine();
        if (line.equals(VALID_FLAG)) {
            StringBuilder xmlString = new StringBuilder();
            while ((line = randomAccessFile.readLine()) != null
                    && !(line.equals(VALID_FLAG) || line.equals(INVALID_FLAG))) {
                xmlString.append("\n");
                xmlString.append(line);
            }
            return xmlString.toString();
        } else {
            return null;
        }
    }

    private void writeStringAtEnd(String string) throws IOException {
        randomAccessFile.seek(eofOffset);
        randomAccessFile.writeBytes(VALID_FLAG + "\n");
        randomAccessFile.writeBytes(string);
        randomAccessFile.writeByte('\n');
        eofOffset = randomAccessFile.getFilePointer();
    }

    private T xmlToInstance(String xmlString) {
        @SuppressWarnings("unchecked")
        T instance = (T) xstream.fromXML(xmlString);
        return instance;
    }

    private String instanceToXml(T instance) throws IOException {
        return xstream.toXML(instance);
    }

    /**
     * Get an ArrayList of IDs of all valid instances.
     *
     * @return the list of all valid IDs
     */
    public ArrayList<Long> getValidIdList() {
        return new ArrayList<Long>(validInstancesMap.keySet());
    }

    /**
     * Get an ArrayList of IDs of all invalid instances.
     *
     * @return the list of all invalid IDs
     */
    public ArrayList<Long> getInvalidIdList() {
        return new ArrayList<Long>(invalidInstancesMap.keySet());
    }

    /**
     * Write a new instance to the database.
     *
     * @param instance the new instance to be inserted
     * @return ID of the inserted instance
     * @throws IOException
     */
    public long putInstance(T instance) throws IOException {
        long instanceId = createNewId();
        validInstancesMap.put(instanceId, eofOffset);
        writeStringAtEnd(instanceToXml(instance));
        return instanceId;
    }

    /**
     * Get the instance with the given ID from the database.
     *
     * @param instanceId The ID of instance to be fetched.
     * @return The reconstructed instance. Note that it is not the same object
     *         with the one that was written to the file. If the ID does not
     *         exist, null will be returned.
     * @throws IOException
     */
    public T getInstance(long instanceId) throws IOException {
        if (!validInstancesMap.containsKey(instanceId)) {
            if (invalidInstancesMap.containsKey(instanceId)) {
                throw new IndexOutOfBoundsException("Instance is invalid.");
            } else {
                throw new IndexOutOfBoundsException("Instance doe not exist.");
            }
        }
        return xmlToInstance(getStringAtOffset(validInstancesMap.get(instanceId)));
    }

    /**
     * Mark the instance with the given ID as invalid.
     *
     * @param instanceId the ID of the instance to be marked.
     * @throws IndexOutOfBoundsException if the ID is not found (or it is
     *             already invalid)
     * @throws IOException
     */
    public void markAsInvalid(long instanceId) throws IOException {
        if (!validInstancesMap.containsKey(instanceId)) {
            throw new IndexOutOfBoundsException();
        }
        long offset = validInstancesMap.get(instanceId);
        randomAccessFile.seek(offset);
        String line = randomAccessFile.readLine();
        if (line.equals(VALID_FLAG)) {
            randomAccessFile.seek(offset);
            randomAccessFile.writeBytes(INVALID_FLAG);
            validInstancesMap.remove(instanceId);
            invalidInstancesMap.put(instanceId, offset);
        } else {
            throw new AssertionError(); // TODO
        }
    }

    /**
     * Mark the instance with the given ID as valid.
     *
     * @param instanceId the ID of the instance to be marked.
     * @throws IndexOutOfBoundsException if the ID is not found (or it is
     *             already valid)
     * @throws IOException
     */
    public void markAsValid(long instanceId) throws IOException {
        if (!invalidInstancesMap.containsKey(instanceId)) {
            throw new IndexOutOfBoundsException();
        }
        long offset = invalidInstancesMap.get(instanceId);
        randomAccessFile.seek(offset);
        String line = randomAccessFile.readLine();
        if (line.equals(INVALID_FLAG)) {
            randomAccessFile.seek(offset);
            randomAccessFile.writeBytes(VALID_FLAG);
            invalidInstancesMap.remove(instanceId);
            validInstancesMap.put(instanceId, offset);
        } else {
            throw new AssertionError(); // TODO
        }
    }

    /**
     * Check whether an ID exists (either represents a valid or invalid
     * instance).
     *
     * @param instanceId the ID to be checked
     * @return if the ID exists
     */
    public boolean contains(long instanceId) {
        return (isValidId(instanceId) || isInvalidId(instanceId));
    }

    /**
     * Check whether an ID represents a valid instance.
     *
     * @param instanceId the ID to be checked
     * @return if the ID represents a valid instance.
     */
    public boolean isValidId(long instanceId) {
        return validInstancesMap.containsKey(instanceId);
    }

    /**
     * Check whether an ID represents an invalid instance.
     *
     * @param instanceId the ID to be checked
     * @return if the ID represents an invalid instance.
     */
    public boolean isInvalidId(long instanceId) {
        return invalidInstancesMap.containsKey(instanceId);
    }

    public void recordAction(Long previousId, Long newId, String description) {
        journal.recordAction(previousId, newId, description);
    }

    public String undo() throws IOException, UnsupportedOperationException {
        return journal.undo();
    }

    public String redo() throws IOException, UnsupportedOperationException {
        return journal.redo();
    }

}
