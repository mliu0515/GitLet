package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.HashMap;
import java.util.List;

/**A commit object represents a commit.
 * @author May Liu*/
public final class Commit implements Serializable {

    /**The other constructor. Arguments will be fixed later.
     * @param logMessage the message.
     * @param parent the SHA of parent.*/
    public Commit(String logMessage, String parent) {
        _logMessage = logMessage;
        _parent = parent;
        _parents.add(parent);
        if (parent == null) {
            SimpleDateFormat fmt =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZZZ");
            fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date theBeginning = new Date(0);
            _timeStamp = fmt.format(theBeginning);
            _shaCode = generateSHA();
            changeStatus(true);
        } else {
            File momFile = new File(Main.ALL_COMMITS, parent);
            Commit mom = Utils.readObject(momFile, Commit.class);
            for (String entry: mom._files.keySet()) {
                _files.put(entry, mom._files.get(entry));
                _filesToModify.put(entry, mom._files.get(entry));
            }
            _timeStamp = generateDate();
        }
    }

    /** Commit with a SECONDPARENT, and LOGMESSAGE. Assert PARENT is not null.*/
    public Commit(String logMessage, String parent, String secondParent) {
        this(logMessage, parent);
        _secondParent = secondParent;
        _parents.add(secondParent);
    }

    /**Return the second parent.*/
    public String getSecondParent() {
        return _secondParent;
    }

    /**Generate the Date of this commit. Return a date String.*/
    private String generateDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat f =
                new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZZZ");
        String result = f.format(date);
        return result;
    }

    /**Get All the Files in the commit. Return the file HashMap.*/
    public HashMap<String, Blob> getFile() {
        return _files;
    }

    /**Return the other HashMap. IDK why this is here but lol.*/
    public HashMap<String, Blob> getModifiableFile() {
        HashMap<String, Blob> result = new HashMap<>(_files);
        return result;
    }

    /**Set the files. given the ADDINGFILES, aka index.
     * And REMOVINGFILES, which is the removing stage*/
    public void setFiles(File addingFiles, File removingFiles) {
        if (!_isCommited) {
            List<String> addingStage = Utils.plainFilenamesIn(addingFiles);
            List<String> deletingStage = Utils.plainFilenamesIn(removingFiles);
            if (addingStage != null) {
                for (String fileName: addingStage) {
                    File currFile = Utils.join(Repository.INDEX, fileName);
                    Blob curBlob = new Blob(currFile);
                    _files.put(fileName, curBlob);
                    _filesToModify.put(fileName, curBlob);
                }
            }
            if (deletingStage != null) {
                for (String fileName: deletingStage) {
                    _files.remove(fileName);
                    _filesToModify.remove(fileName);
                }
            }

        }
        _shaCode = generateSHA();
    }

    /**Generate the SHA of the commit.
     * Return the SHA code in String form.*/
    private String generateSHA() {
        return Utils.sha1(_files.toString(), _logMessage, _timeStamp);
    }

    /**Change the status of the commit to ISCOMMITTED. if ISCOMMITED.*/
    public void changeStatus(Boolean isCommited) {
        if (!_isCommited) {
            _isCommited = isCommited;
        }
    }

    /**Getter method.
     * Return the parent.
     * The first parent.*/
    public String getParent() {
        return _parent;
    }

    /**Getter method. Get the log Message.
     * Return the String representing the log message.*/
    public String getLogMsg() {
        return _logMessage;
    }

    /**Getter method. Return the String of SHA code.*/
    public String getShaCode() {
        return _shaCode;
    }

    /**Return the String of timestamp.
     * It has a format.*/
    public String getTimeStamp() {
        return _timeStamp;
    }

    /**Return the arrayList of parents. Return a List of parents.*/
    public ArrayList<String> parents() {
        return _parents;
    }

    /**Get the content of the file inside this commit given the FILENAME.
     * Return a String of file's content.*/
    public String getFileContent(String fileName) {
        Blob blb = _files.get(fileName);
        if (blb != null) {
            return blb.getContent();
        }
        return null;
    }

    /**The log message of the commit.*/
    private final String _logMessage;

    /**The SHA. Hehe.*/
    private String _shaCode;

    /**A list of files contained in this commit.
     * Should I make it a stack?
     * What if instead of blobs, we have a String?
     * String is probably the name of the file?
     * And the Blob is just the content of it.*/
    private final HashMap<String, Blob> _files = new HashMap<>();

    /**Honestly idk why this is here but OK.*/
    private HashMap<String, Blob> _filesToModify = new HashMap<>();

    /**The parent commit.
     * The String represents the SHA code of the parent commit.*/
    private String _parent;

    /**A list of parents. Used during merge.*/
    private ArrayList<String> _parents = new ArrayList<>();

    /**The second parent commit if it has one.*/
    private String _secondParent;

    /**Check if this commit is already commited.
     * If yes, then we can't modify things anymore!
     * Starts off with false*/
    private boolean _isCommited = false;

    /**The TimeStemp.*/
    private final String _timeStamp;

    private static final long serialVersionUID = 0;

}
