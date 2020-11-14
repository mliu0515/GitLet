package gitlet;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

/**This is a repository. It does stuff.
 * @author May Liu*/
public class Repository {

    /**the directory name of the repository.*/
    static final File BLOBS = Utils.join(Main.DOTFILE, "blobs");
    /**Master branch.*/
    static final File MASTERBRANCH = new File(Main.ALL_BRANCHES, "master");
    /**head file.*/
    static final File HEADFILE = new File(Main.DOTFILE, "head");
    /**index file. Adding stage.*/
    static final File INDEX = new File(Main.DOTFILE, "index");
    /**removing stage.*/
    static final File REMOVAL = new File(Main.DOTFILE, "removingStage");
    /**the file contains name of HEAD branch.*/
    static final File HEADNAME = new File(Main.DOTFILE, "headName");
    /**each commit has at most 2 parents.*/
    static final int NUMPARENTS = 2;

    /**A constructor for Repository.
     * Input: the directory of the repo.
     * Construct a new repo. This includes:
     * 1. Creating an empty .gitlet directory
     * 2. let the DotFile do the rest.*/
    public Repository()  {

    }

    /**Set up the persistance, what can I say.*/
    public void setPersistance() throws IOException {
        createDir(BLOBS);
        createDir(REMOVAL);
        createDir(INDEX);
        createDir(Main.ALL_BRANCHES);
        createDir(Main.ALL_COMMITS);
        createFile(MASTERBRANCH);
        createFile(HEADFILE);
        createFile(HEADNAME);
    }

    /**Create a File given the name of the FILE.*/
    public void createFile(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    /**This helps create a directory if the file does not exist.
     * given FILE.*/
    public void createDir(File file) {
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /**Initialize the rest of the repo.*/
    public void init() throws IOException {
        setPersistance();
        Commit newCommit = new Commit("initial commit", null);
        String newCommitCode = newCommit.getShaCode();
        File lol = new File(Main.ALL_COMMITS, newCommitCode);
        lol.createNewFile();
        if (lol.exists()) {
            Utils.writeObject(lol, newCommit);
        }
        Utils.writeObject(MASTERBRANCH, newCommitCode);
        Utils.writeObject(HEADFILE, newCommitCode);
        Utils.writeObject(HEADNAME, MASTERBRANCH.getName());
    }

    /**It does the job of adding.
     * Extract the staging area from the index file
     * modify if by adding another FILE name Blob pair.
     * Put the newly modified staging back to the index file.
     * Question: will this override the file or add a new one?
     * If the current working version of the file is
     * identical to the version in the current commit,
     * do not stage it to be added, and remove it
     * from the staging area if it is already there.*/
    public void add(File file) throws IOException {
        File realFile = new File(Main.CWD, file.getName());
        File rmStage = new File(REMOVAL, file.getName());
        if (rmStage.exists()) {
            rmStage.delete();
        }
        if (realFile.exists()) {
            if (curAndCWDIdentical(file, realFile)) {
                File stagingFile = new File(INDEX, file.getName());
                if (stagingFile.exists()) {
                    stagingFile.delete();
                }
            } else {
                File resultingFile = new File(INDEX, file.getName());
                if (!resultingFile.exists()) {
                    resultingFile.createNewFile();
                }
                String content = Utils.readContentsAsString(realFile);
                Utils.writeContents(resultingFile, content);
            }
        }
    }

    /**Check If the ADDEDFILE is identical REALFILE.
     * Return true if they are identical.
     * Return false if otherwise.*/
    public boolean curAndCWDIdentical(File addedFile, File realFile) {
        String currContent = Utils.readContentsAsString(realFile);
        Commit headCommit = getHeadCommit();
        Blob blobInHead = headCommit.getFile().get(addedFile.getName());
        if (blobInHead == null) {
            return false;
        }
        String contentInHead = blobInHead.getContent();
        if (contentInHead == null) {
            return false;
        }
        if (currContent.equals(contentInHead)) {
            return true;
        }
        return false;
    }

    /**Return the current head branch file.*/
    public File curHeadBranch() {
        File result = null;
        String headName = Utils.readObject(HEADNAME, String.class);
        for (String fileName: Utils.plainFilenamesIn(Main.ALL_BRANCHES)) {
            if (fileName.equals(headName)) {
                File currFile = Utils.join(Main.ALL_BRANCHES, fileName);
                result = currFile;
            }
        }
        return result;
    }

    /** First, create a clone of the current head commit.
     * Change the timeStemp and the LOGMSG.
     * Look at the stage, copy all the
     * maps in there to the _file in the Commit class.
     * Delete everything currently existed in the Stage.
     * Generate a SHA code for all these?? */
    public void commit(String logMsg) throws IOException {
        if (Utils.plainFilenamesIn(INDEX).size() == 0
                && Utils.plainFilenamesIn(REMOVAL).size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        File head = new File(Main.DOTFILE, "head");
        if (head.exists()) {
            String parentCommit = Utils.readObject(head, String.class);
            Commit newCommit = new Commit(logMsg, parentCommit);
            helperCommit(newCommit);
        }
    }

    /**A helper methods that does the rest of the commiting job.
     * After given the NEWCOMMIT, complete the commit.*/
    public void helperCommit(Commit newCommit) throws IOException {
        newCommit.setFiles(INDEX, REMOVAL);
        newCommit.changeStatus(true);
        addCommit(newCommit);
        Utils.writeObject(curHeadBranch(), newCommit.getShaCode());
        Utils.writeObject(HEADFILE, newCommit.getShaCode());
        clearStages();
    }

    /**Add the COMMIT to the branches directory.
     * This is just a helper method.*/
    public void addCommit(Commit commit) throws IOException {
        String fileName = commit.getShaCode();
        File actualFile = new File(Main.ALL_COMMITS, fileName);
        if (!actualFile.exists()) {
            actualFile.createNewFile();
        }
        Utils.writeObject(actualFile, commit);
    }

    /**Clear up the two stages.
     * Do it by overwriting index and removal with empty hashmaps.*/
    public void clearStages() {
        List<String> addingStage = Utils.plainFilenamesIn(INDEX);
        List<String> deletingStage = Utils.plainFilenamesIn(REMOVAL);
        for (String fileName: addingStage) {
            File currFile = new File(INDEX, fileName);
            currFile.delete();
        }
        for (String fileName: deletingStage) {
            File currFile = new File(REMOVAL, fileName);
            currFile.delete();
        }
    }

    /**Print the logMessage of the .gieLet file.
     * NOTE: not the global-log!!!*/
    public void showLogs() {
        String headID = Utils.readObject(HEADFILE, String.class);
        File asFile = new File(Main.ALL_COMMITS, headID);
        Commit associatedCommit = Utils.readObject(asFile, Commit.class);
        while (associatedCommit != null) {
            printCommitLog(associatedCommit);
            String parent = associatedCommit.getParent();
            if (parent == null) {
                break;
            }
            File commitFile = new File(Main.ALL_COMMITS, parent);
            associatedCommit = Utils.readObject(commitFile, Commit.class);
        }
    }

    /**Print of the global log of the repo.
     * The order of the commits does not matter.*/
    public void showGlobal() {
        List<String> allCommits = getAllCommits();
        for (String cmt: allCommits) {
            File commitFile = new File(Main.ALL_COMMITS, cmt);
            Commit associatedCommit =
                    Utils.readObject(commitFile, Commit.class);
            printCommitLog(associatedCommit);
        }
    }

    /**Print out the log of a single COMMIT.*/
    public void printCommitLog(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getShaCode());
        if (commit.parents().size() == NUMPARENTS) {
            String mom = commit.getParent();
            mom = mom.substring(0, 7);
            String dad = commit.getSecondParent();
            dad = dad.substring(0, 7);
            System.out.println("Merge: " + mom + " " + dad);
        }
        System.out.println("Date: " + commit.getTimeStamp());
        System.out.println(commit.getLogMsg() + "\n");
    }

    /**Print of the current status of the Repo.*/
    public void printStatus() {
        printBranches();
        printAllStages();
        printModified();
        printUntracked();
    }

    /** Display what branches currently exist.
     * Marks the current branch with a "*".*/
    public void printBranches() {
        System.out.println("=== Branches ===");
        List<String> allBranchF = Utils.plainFilenamesIn(Main.ALL_BRANCHES);
        String commitInHead = Utils.readObject(HEADNAME, String.class);
        for (String branch: allBranchF) {
            if (branch.equals(commitInHead)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
    }

    /** Display what files have been staged for addition or removal.*/
    public void printAllStages() {
        printIndex();
        printRemoval();
    }

    /**Print out a list of files that are staged in the Index file.*/
    public void printIndex() {
        System.out.println("\n" + "=== Staged Files ===");
        List<String> addingStage = Utils.plainFilenamesIn(INDEX);
        for (String filesName: addingStage) {
            File fileInIndex = new File(INDEX, filesName);
            File fileInCWD = new File(Main.CWD, filesName);
            if (Utils.readContentsAsString(fileInCWD)
                    .equals(Utils.readContentsAsString(fileInIndex))) {
                System.out.println(filesName);
            }
        }
    }

    /**Print out a list of files that are staged for removal.*/
    public void printRemoval() {
        System.out.println("\n" + "=== Removed Files ===");
        List<String> deleting = Utils.plainFilenamesIn(REMOVAL);
        for (String filesName: deleting) {
            System.out.println(filesName);
        }
    }

    /**Print out a list of files that are modified but not staged.
     * Runtime should only depend on the amount of files in CWD.
     * 1. Tracked in the current commit, changed in the CWD, but not staged.
     * 2. Staged for addition, but with different contents than CWD.
     * 3. Staged for addition, but deleted in the working directory.
     * 4. Not staged for removal, but tracked in the current commit
     * and deleted from the working directory. (ie, if the file
     * is present only in the current commit and no where else.)/
     * Also all the files are in lexicographic order.*/
    public void printModified() {
        System.out.println("\n"
                + "=== Modifications Not Staged For Commit ===");
    }

    /**Check if the file named FILENAME exists in the index.
     * Return true if it exists, return false if not.*/
    private boolean isStaged(String fileName) {
        List<String> addingStage = Utils.plainFilenamesIn(INDEX);
        if (addingStage != null) {
            if (addingStage.contains(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**Tracked in the curr cmt, changed in the CWD but not staged.
     * Return a List of Strings that represents files in ALLFILES
     * that satisfies the above condition.*/
    private List<String> newChangNotStaged(HashMap<String, Blob> allFiles) {
        ArrayList<String> result = new ArrayList<>();
        for (String fileName: Utils.plainFilenamesIn(Main.CWD)) {
            Blob ourBlob = allFiles.get(fileName);
            File fileInCWD = new File(Main.CWD, fileName);
            String content = Utils.readContentsAsString(fileInCWD);
            if (ourBlob != null) {
                if (!ourBlob.getContent().equals(content)
                        && !isStaged(fileName)) {
                    String elem = fileName + " (modified)";
                    result.add(elem);
                }
                allFiles.remove(fileName);
            }
        }
        return result;
    }

    /** Now after looking at what's not staged, look at what is staged.
     * 2. Staged for addition, but with different contents
     * than in the working directory.
     * 3. Staged for addition, but deleted in the working directory.
     * Return ArrayList of file names.*/
    private ArrayList<String> stagedButDiffInCWD() {
        ArrayList<String> result = new ArrayList<>();
        List<String> addingStage = Utils.plainFilenamesIn(INDEX);
        for (String file: addingStage) {
            File fileCWD = new File(Main.CWD, file);
            File stagedFile = new File(INDEX, file);
            if (!fileCWD.exists()) {
                String message = file + " (deleted)";
                result.add(message);
            } else {
                String contentOne = Utils.readContentsAsString(fileCWD);
                String contentTwo = Utils.readContentsAsString(stagedFile);
                if (!contentOne.equals(contentTwo)) {
                    String message = file + " (modified)";
                    result.add(message);
                }
            }
        }
        return result;
    }

    /**Not staged for rm, but tracked in the curr cmt and deleted from the CWD.
     * Add all the files tin ALLFILES
     * that satisfied this condition.
     * Return a list of String filenames.*/
    public ArrayList<String> deletedButNotStaged(
            HashMap<String, Blob> allFiles) {
        ArrayList<String> result = new ArrayList<>();
        for (String fileName: Utils.plainFilenamesIn(Main.CWD)) {
            if (allFiles.get(fileName) != null) {
                allFiles.remove(fileName);
            }
        }
        List<String> deletingStage = Utils.plainFilenamesIn(REMOVAL);
        for (String fileName: deletingStage) {
            if (allFiles.get(fileName) != null) {
                allFiles.remove(fileName);
            }
        }
        Set<String> keySet = allFiles.keySet();
        for (String file: keySet) {
            result.add(file + " (deleted)");
        }
        return result;
    }

    /**The final category ("Untracked Files").
     * It is for files present in the working directory.
     * But neither staged for addition nor tracked.
     * This includes files that have been staged for removal,
     * but then re-created without Gitlet's knowledge.*/
    public void printUntracked() {
        System.out.println("\n" + "=== Untracked Files ===");
    }

    /**Take the version of the FILENAME as inside commit with the given ID.
     * Put it in the working directory.
     * Overwriting the version of the file if there is already one.*/
    public void commitIDCheckout(String id,
                                 String fileName) throws IOException {
        File commitFile = new File(Main.ALL_COMMITS, id);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            Commit cmt = Utils.readObject(commitFile, Commit.class);
            if (!cmt.getFile().keySet().contains(fileName)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
            processCheckout(commitFile, fileName);
        }
    }

    /**Takes the version of the FILENAME as it exists in the head commit.
     * Puts it in the working directory, overwriting the version
     * of the file that's already there if there is one.*/
    public void fileNameCheckOut(String fileName) throws IOException {
        String id = getHeadCommitID();
        File commitFile = new File(Main.ALL_COMMITS, id);
        processCheckout(commitFile, fileName);
    }

    /**This is just a helper method that helps checking out.
     * given COMMITFILE and the FILENAME inside COMMITFILE,
     * checkout accordingly.*/
    public void processCheckout(File commitFile,
                                String fileName) throws IOException {
        Commit associatedCommit = Utils.readObject(commitFile, Commit.class);
        HashMap<String, Blob> allfilesInIt = associatedCommit.getFile();
        Blob fileContent = allfilesInIt.get(fileName);
        File fileinCWD = new File(Main.CWD, fileName);
        if (!fileinCWD.exists()) {
            fileinCWD.createNewFile();
        }
        Utils.writeContents(fileinCWD, fileContent.getContent());
    }

    /**Get the head commit of the current state.
     * Return the head commit String SHA code.*/
    public String getHeadCommitID() {
        return Utils.readObject(HEADFILE, String.class);
    }

    /**Get the current Commit that the head pointer points to.
     * Return the head Commit.*/
    public Commit getHeadCommit() {
        Commit result = null;
        File headFile = new File(Main.DOTFILE, HEADNAME.getName());
        if (headFile.exists()) {
            String id = Utils.readObject(HEADNAME, String.class);
            File associatedFile = new File(Main.ALL_BRANCHES, id);
            if (associatedFile.exists()) {
                String headID = Utils.readObject(associatedFile, String.class);
                File cmt = new File(Main.ALL_COMMITS, headID);
                result = Utils.readObject(cmt, Commit.class);
            }
        }
        return result;
    }

    /**Unstage the FILE if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal.*/
    public void remove(File file) throws IOException {
        String fileName = file.getName();
        File realFile = new File(INDEX, fileName);
        boolean isStaged = realFile.exists();
        Commit curCommit = getHeadCommit();
        if (isStaged) {
            realFile.delete();
        }
        HashMap<String, Blob> filesInHeadCommit = curCommit.getFile();
        boolean trackedByCurrCommit = filesInHeadCommit.containsKey(fileName);
        if (trackedByCurrCommit) {
            if (file.exists()) {
                file.delete();
            }
            String content = filesInHeadCommit.get(fileName).getContent();
            File fileInRemovingStage = new File(REMOVAL, fileName);
            if (!fileInRemovingStage.exists()) {
                fileInRemovingStage.createNewFile();
            }
            Utils.writeContents(fileInRemovingStage, content);
        }
        if (!isStaged && !trackedByCurrCommit) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

    }

    /**Prints out the ids of all commits that have LOGMSG.
     * If no such commit exists, prints the error.*/
    public void findCommits(String logMsg) {
        List<String> allCommitID = getAllCommits();
        int countTotal = 0;
        for (String cmt: allCommitID) {
            File commitFile = new File(Main.ALL_COMMITS, cmt);
            Commit associatedCommit =
                    Utils.readObject(commitFile, Commit.class);
            if (associatedCommit.getLogMsg().equals(logMsg)) {
                System.out.println(cmt);
                countTotal = countTotal + 1;
            }
        }
        if (countTotal == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**Get a List of All commit ID's ever made.
     * Return a list of all String SHAcode.*/
    public List<String> getAllCommits() {
        return Utils.plainFilenamesIn(Main.ALL_COMMITS);
    }

    /**Takes all files in the commit at the head of the given BRANCHNAME.
     * Put them in the working directory.
     * overwriting the versions of the files already there if they exist.*/
    public void checkOutBranch(String branchName) throws IOException {
        File branch = new File(Main.ALL_BRANCHES, branchName);
        if (branch.exists()) {
            String associatedID = Utils.readObject(branch, String.class);
            String commitID = Utils.readObject(HEADFILE, String.class);
            File curCommitFile = new File(Main.ALL_COMMITS, commitID);
            Commit currCommit = Utils.readObject(curCommitFile, Commit.class);
            if (branchName.equals(Utils.readObject(HEADNAME, String.class))) {
                System.out.println("No need to checkout the current branch.");
            } else {
                HashMap<String, Blob> allFiles =
                        getCommitFromID(associatedID).getFile();
                HashMap<String, Blob> filesInCurr = currCommit.getFile();
                checkOutFiles(allFiles, filesInCurr);
                deleteIfNecessary(allFiles, filesInCurr);
                Utils.writeObject(HEADFILE, associatedID);
                Utils.writeObject(HEADNAME, branchName);
            }
        } else {
            System.out.println("No such branch exists.");
        }
    }

    /**Check if there's untracked files when adding.
     * FILENAME, CUR are the two files were comparing.*/
    private void checkUntracked(String fileName, HashMap<String, Blob> cur) {
        File fileInCWD = new File(Main.CWD, fileName);
        if (fileInCWD.exists() && (cur.get(fileName) == null
                || !cur.get(fileName).getContent()
                .equals(Utils.readContentsAsString(fileInCWD)))) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    /**Move all the files in the keyset of ALLFILES to the CWD.
     * Either create or override. Depending on the situation.
     * what is CURFILE? IDK.*/
    private void checkOutFiles(HashMap<String, Blob> allFiles,
                               HashMap<String, Blob> curFile)
            throws IOException {
        for (String fileName: allFiles.keySet()) {
            File fileInCWD = new File(Main.CWD, fileName);
            String content = allFiles.get(fileName).getContent();
            checkUntracked(fileName, curFile);
            if (!fileInCWD.exists()) {
                fileInCWD.createNewFile();
            }
            Utils.writeContents(fileInCWD, content);
        }
        for (String fileName: curFile.keySet()) {
            Blob ourBlob = allFiles.get(fileName);
            if (ourBlob == null) {
                File fileInCWD = new File(Main.CWD, fileName);
                if (fileInCWD.exists()) {
                    fileInCWD.delete();
                }
            }
        }
    }

    /**Dekete the file if it exists in FILESINCURR but not in ALLFILES.*/
    private void deleteIfNecessary(HashMap<String, Blob> allFiles,
                                   HashMap<String, Blob> filesInCurr) {
        for (String fileName: filesInCurr.keySet()) {
            if (allFiles.get(fileName) == null) {
                File fileInCWD = new File(Main.CWD, fileName);
                if (fileInCWD.exists()) {
                    fileInCWD.delete();
                }
            }
        }
    }

    /**Create a new branch named after BRANCHNAME.
     * Put it in the branches folder.
     * points it at the current head node*/
    public void createNewBranch(String branchName) throws IOException {
        File newBranch = new File(Main.ALL_BRANCHES, branchName);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            newBranch.createNewFile();
            String headCommitID = getHeadCommitID();
            Utils.writeObject(newBranch, headCommitID);
        }
    }

    /**Remove a branch with name of BRANCHNAME.*/
    public void removeBranch(String branchName) {
        File removedBranch = new File(Main.ALL_BRANCHES, branchName);
        if (!removedBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else {
            if (branchName.equals(Utils.readObject(HEADNAME, String.class))) {
                System.out.println("Cannot remove the current branch.");
            } else {
                removedBranch.delete();
            }
        }
    }

    /**Overwrites tracked files to those in COMMITID.
     The command is essentially checkout of an arbitrary
     commit that also changes the current branch head.*/
    public void reset(String commitID) throws IOException {
        File f = new File(Main.ALL_COMMITS, commitID);
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit referencedCommit = getCommitFromID(commitID);
        checkUntracked(referencedCommit);
        Set<String> fls = referencedCommit.getFile().keySet();
        for (String file: getHeadCommit().getFile().keySet()) {
            if (referencedCommit.getFile().get(file) == null) {
                File theFile = new File(Main.CWD, file);
                if (theFile.exists()) {
                    theFile.delete();
                }
            }
        }
        for (String elem: fls) {
            commitIDCheckout(commitID, elem);
        }
        String curBID = getHeadCommitID();
        String currBranchName = "";
        for (String branchName: Utils.plainFilenamesIn(Main.ALL_BRANCHES)) {
            File branchFile = new File(Main.ALL_BRANCHES, branchName);
            if (Utils.readObject(branchFile, String.class).equals(curBID)) {
                currBranchName = branchName;
                break;
            }
        }
        File branchFile = new File(Main.ALL_BRANCHES, currBranchName);
        Utils.writeObject(branchFile, commitID);
        Utils.writeObject(HEADFILE, commitID);
        Utils.writeObject(HEADNAME, currBranchName);
        clearStages();
    }

    /**Merge two files!!!
     * First branch: BRANCHNAME.
     * Second brandh: the HEAD branch.
     * More detailed spec coming soon.*/
    public void merge(String branchName) throws IOException {
        if (!Utils.plainFilenamesIn(INDEX).isEmpty()
                || !Utils.plainFilenamesIn(REMOVAL).isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        File givenBranch = new File(Main.ALL_BRANCHES, branchName);
        if (!givenBranch.exists()) {
            System.out.println(" A branch with that name does not exist.");
            System.exit(0);
        }
        String currentCommit = Utils.readObject(HEADFILE, String.class);
        String givenCommitID = Utils.readObject(givenBranch, String.class);
        if (givenCommitID.equals(currentCommit)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Commit splitPt = findSplitPt(branchName, new HashSet<>());
        if (splitPt.getShaCode().equals(givenCommitID)) {
            System.out.println("Given branch "
                    + "is an ancestor of the current branch.");
            System.exit(0);
        }
        if (currentCommit.equals(splitPt.getShaCode())) {
            System.out.println("Current branch fast-forwarded.");
            checkOutBranch(branchName);
            System.exit(0);
        }
        boolean conflictPresent = checkConflict(branchName, splitPt);
        String lgMsg = "Merged " + branchName + " into "
                + Utils.readObject(HEADNAME, String.class) + ".";
        String lgWithConflict = "Encountered a merge conflict.";
        Commit newCommit = new Commit(lgMsg, currentCommit,
                Utils.readObject(givenBranch, String.class));
        helperCommit(newCommit);
        if (conflictPresent) {
            System.out.println(lgWithConflict);
            System.exit(0);
        }
    }

    /**Check if there's untracked files in current commit and GIVENCOMMIT.*/
    public void checkUntracked(Commit givenCommit) {
        for (String fileName: givenCommit.getFile().keySet()) {
            File fileInCWD = new File(Main.CWD, fileName);
            if (fileInCWD.exists()) {
                String contentInCWD = Utils.readContentsAsString(fileInCWD);
                Blob curBlob = getHeadCommit().getFile().get(fileName);
                if (curBlob == null) {
                    if (!contentInCWD.equals(givenCommit.getFile()
                            .get(fileName))) {
                        System.out.println("There is an "
                                + "untracked file in the way;"
                                + " delete it, or add and commit it first.");
                        System.exit(0);
                    }
                } else if (curBlob != null) {
                    String contentInCurr = curBlob.getContent();
                    if (!contentInCurr.equals(contentInCWD)
                            && !contentInCWD.equals(givenCommit.getFile()
                            .get(fileName))) {
                        System.out.println("There is "
                                + "an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
    }

    /**Look at the givenbranch BRANCHNAME, and the SPLITPT.
     * Return true if there is a conflict.*/
    private boolean checkConflict(String branchName,
                                  Commit splitPt) throws IOException {
        Commit givenCommit = findCommit(branchName);
        checkUntracked(givenCommit);
        Commit currCommit = getHeadCommit();
        HashMap<String, Blob> givenFile = givenCommit.getFile();
        checkSplitPoint(splitPt, currCommit, givenCommit);
        boolean conflictInGiven = processGivenFile(givenFile, splitPt,
                currCommit, givenCommit);
        boolean conflictInCurr = processCurrFile(currCommit.getFile(),
                splitPt, currCommit, givenCommit);
        return (conflictInCurr || conflictInGiven);
    }

    /**Process the currFile. All it does is to check it there's conflict.
     * Return true if there is conflict.
     * Given CURRFILE, SPLITPT, CURRCOMMIT, and GIVENCOMMIT.*/
    private boolean processCurrFile(HashMap<String, Blob> currFile,
                                    Commit splitPt, Commit currCommit,
                                    Commit givenCommit) {
        for (String fileName: currFile.keySet()) {
            String contentInSp = splitPt.getFileContent(fileName);
            String contentInCur = currCommit.getFileContent(fileName);
            String contentInGiven = givenCommit.getFileContent(fileName);
            if (isInConflict(contentInCur, contentInGiven, contentInSp)) {
                generateConflict(fileName, contentInCur, contentInGiven);
                return true;
            }
        }
        return false;
    }
    /**Just an ugly helper method that helps processing the file.
     * Process GIVENFILE, see the relationship between
     * CURRCOMMIT, SPLITPT, and GIVENCOMMIT.
     * Return true if conflict is present.*/
    private boolean processGivenFile(HashMap<String, Blob> givenFile,
                                     Commit splitPt, Commit currCommit,
                                     Commit givenCommit) throws IOException {
        for (String fileName: givenFile.keySet()) {
            String contentInSp = splitPt.getFileContent(fileName);
            String contentInCur = currCommit.getFileContent(fileName);
            String contentInGiven = givenCommit.getFileContent(fileName);
            if (isInConflict(contentInCur, contentInGiven, contentInSp)) {
                generateConflict(fileName, contentInCur, contentInGiven);
                return true;
            } else if (bothNull(contentInCur, contentInGiven)
                    || contentInGiven.equals(contentInCur)) {
                continue;
            }
            if (contentInSp != null && contentInSp.equals(contentInCur)
                    && !contentInSp.equals(contentInGiven)) {
                commitIDCheckout(givenCommit.getShaCode(), fileName);
                File theNewFile = new File(Main.CWD, fileName);
                add(theNewFile);
            } else if (contentInCur == null && contentInSp == null) {
                commitIDCheckout(givenCommit.getShaCode(), fileName);
                File theNewFile = new File(Main.CWD, fileName);
                add(theNewFile);
            }
        }
        return false;
    }

    /**Check if the file in the SPLITPT.
     * See if it's present in CURRCOMMIT but not in GIVENCOMMIT.
     * If that is the case, stage it for removal.*/
    private void checkSplitPoint(Commit splitPt, Commit currCommit,
                                 Commit givenCommit) throws IOException {
        HashMap<String, Blob> files = splitPt.getFile();
        for (String fileName: files.keySet()) {
            String contentInSp = splitPt.getFileContent(fileName);
            String contentInCur = currCommit.getFileContent(fileName);
            String contentInGiven = givenCommit.getFileContent(fileName);
            if (contentInSp != null && contentInSp.equals(contentInCur)
                    && contentInGiven == null) {
                File thisLilFile = new File(Main.CWD, fileName);
                remove(thisLilFile);
            }
        }
    }

    /**A helper method that checks whether there's conflict.
     * Observes contents in CONTENTINCUR, CONTENTINGIVEN,
     * and CONTENTINSP.
     * Return true if there is, false if otherwise.*/
    private boolean isInConflict(String contentInCur,
                                 String contentInGiven, String contentInSp) {
        if (bothModified(contentInCur, contentInGiven, contentInSp)) {
            if (isDifferent(contentInGiven, contentInCur)) {
                return true;
            }
        }
        return false;
    }

    /**Return a boolean.
     * Show whether both CONTENTINGIVEN and CONTENTINCUR
     * are different from CONTENTINSP.
     * Return true if the two are both modified.
     * Return false if only one or none of them are modified.*/
    private boolean bothModified(String contentInCur,
                                 String contentinGiven, String contentInSp) {
        if (contentInSp == null && contentinGiven != null
                && contentInCur != null) {
            return true;
        } else if (contentInSp != null && !contentInSp.equals(contentInCur)
                && !contentInSp.equals(contentinGiven)) {
            return true;
        }
        return false;
    }

    /**Checks whether the CONTENTINGIVEN deffers from CONTENTINCURR.
     * In this sitaution when the function is called:
     * if the two files have different content, then there's a conflict.
     * Return true is the two are different, false if otherwise.*/
    private boolean isDifferent(String contentInGiven, String contentInCurr) {
        if (contentInGiven == null && contentInCurr == null) {
            return false;
        } else if (contentInCurr != null && contentInGiven != null
                && contentInCurr.equals(contentInGiven)) {
            return false;
        }
        return true;
    }

    /**Generate the content of the conflict file.
     * HEADCONTENT and GIVENCONTENT are the content.
     * FILENAME indicate the filename of the filew in CWD.*/
    private void generateConflict(String fileName,
                                  String headContent, String givenContent) {
        File theFile = new File(Main.CWD, fileName);
        if (givenContent == null) {
            givenContent = "";
        }
        if (headContent == null) {
            headContent = "";
        }
        String mes =  "<<<<<<< HEAD\n" + headContent
                + "=======\n" + givenContent + ">>>>>>>\n";
        Utils.writeContents(theFile, mes);
    }

    /**Find the split point in the BRANCHNAME.
     * MARKEDITEMS is a fringe.
     * Return the split point Commit.*/
    private Commit findSplitPt(String branchName, HashSet<String> markedItems) {
        Stack<String> fringe = new Stack<>();
        fringe.push(getHeadCommitID());
        fringe.push(findCommit(branchName).getShaCode());
        while (!fringe.isEmpty()) {
            String v = fringe.pop();
            Commit associatedCommit = getCommitFromID(v);
            if (markedItems.contains(v)) {
                return associatedCommit;
            } else {
                markedItems.add(v);
                for (String parent: associatedCommit.parents()) {
                    if (parent != null) {
                        fringe.push(parent);
                    }
                }
            }
        }
        return null;
    }

    /**Return the Commit given the SHACODE.*/
    private Commit getCommitFromID(String shaCode) {
        File f = new File(Main.ALL_COMMITS, shaCode);
        if (f.exists()) {
            return Utils.readObject(f, Commit.class);
        } else {
            return null;
        }
    }

    /**Return the Commit inside BRANCHNAME.*/
    private Commit findCommit(String branchName) {
        File givenBranch = new File(Main.ALL_BRANCHES, branchName);
        if (givenBranch.exists()) {
            String commitID = Utils.readObject(givenBranch, String.class);
            File theCommit = new File(Main.ALL_COMMITS, commitID);
            Commit cmt = Utils.readObject(theCommit, Commit.class);
            return cmt;
        } else {
            return null;
        }
    }

    /**Return true if both ONE and TWO are null.*/
    private boolean bothNull(String one, String two) {
        return (one == null && two == null);
    }
}



