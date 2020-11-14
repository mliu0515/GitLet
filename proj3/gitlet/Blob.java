package gitlet;

import java.io.File;
import java.io.Serializable;

/**Contents of the file.
 * @author May Liu*/
public final class Blob implements Serializable {

    /**Constructor.
     * @param file the file represented by this Blob.*/
    public Blob(File file) {
        _content = Utils.readContentsAsString(file);
        _SHA = Utils.sha1(_content);
    }

    /**Return the String representing the content of the file.*/
    public String getContent() {
        return _content;
    }

    /**See if the two blobs are equal.
     * Return boolean given the ANOTHERBLOB that this blob is comparing to.*/
    public boolean equals(Blob anotherBlob) {
        return _SHA.equals(anotherBlob.getSHA());
    }

    /**Return a string representing the SHA code of the file.*/
    public String getSHA() {
        return _SHA;
    }

    /**The String content.*/
    private String _content;

    /**The SHA code.*/
    private String _SHA;

}
