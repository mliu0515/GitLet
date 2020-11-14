package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author May liu
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    /**current working directory.*/
    static final File CWD = new File(System.getProperty("user.dir"));
    /**.gitlet.*/
    static final File DOTFILE = new File(CWD.getPath(), ".gitlet");
    /**file that stores all branches.*/
    static final File ALL_BRANCHES = new File(DOTFILE, "branches");
    /**file that stores all branches.*/
    static final File ALL_COMMITS = new File(DOTFILE, "allCommits");
    /**initialized repository that connects things together.*/
    static final Repository INITIALIZEDREPO = new Repository();
    /**length of SHA code.*/
    static final int SHASIZE = 40;
    /**Now the main method. checks if ARGS is in a certain format.*/
    public static void main(String... args) throws IOException {
        if (args.length == 0 || args.equals("")) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (args[0].equals("init")) {
            initRepository(args);
        } else if (args[0].equals("add")) {
            addFiles(args);
        } else if (args[0].equals("commit")) {
            makeCommit(args);
        } else if (args[0].equals("rm")) {
            removeFile(args);
        } else if (args[0].equals("log")) {
            showLogs(args);
        } else if (args[0].equals("global-log")) {
            showGlobal(args);
        } else if (args[0].equals("find")) {
            find(args);
        } else if (args[0].equals("status")) {
            printStatus(args);
        } else if (args[0].equals("checkout")) {
            chekout(args);
        } else if (args[0].equals("branch")) {
            createNewBranch(args);
        } else if (args[0].equals("rm-branch")) {
            removeBranch(args);
        } else if (args[0].equals("reset")) {
            reset(args);
        } else if (args[0].equals("merge")) {
            merge(args);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }


    /**Initialise a git repository under the CWD. Given ARGS.
     * The repository name should be the second argument of args.*/
    public static void initRepository(String[] args) throws IOException {
        validateNumArgs("init", args, 1);
        if (DOTFILE.exists()) {
            System.out.println(
                    "A Gitlet version-control system already "
                            + "exists in the current directory.");
            System.exit(0);
        }
        if (!DOTFILE.exists()) {
            DOTFILE.mkdir();
        }
        if (!ALL_BRANCHES.exists()) {
            ALL_BRANCHES.mkdir();
        }
        if (!ALL_COMMITS.exists()) {
            ALL_COMMITS.mkdir();
        }
        INITIALIZEDREPO.init();
    }

    /**When you want to add Blobs into the repo. Basically add
     * Somehow needs to check whether or not the thing is tracked.
     * Given ARGS.*/
    public static void addFiles(String[] args) throws IOException {
        validateNumArgs("add", args, 2);
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        File addedFile = new File(CWD, args[1]);
        if (!addedFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            INITIALIZEDREPO.add(addedFile);
        }
    }

    /**Make a commit.
     * Given ARGS.*/
    public static void makeCommit(String[] args) throws IOException {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args.length == 1 || args[1].equals("")) {
            System.out.println("Please enter a commit message.");
        } else {
            validateNumArgs("commit", args, 2);
            INITIALIZEDREPO.commit(args[1]);
        }
    }

    /**Stage a file to be removed. Given ARGS.*/
    public static void removeFile(String[] args) throws IOException {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("rm", args, 2);
        File removedFile = new File(CWD, args[1]);
        INITIALIZEDREPO.remove(removedFile);
    }

    /**Show the log of the whole repository. Given ARGS.*/
    public static void showLogs(String[] args) {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("log", args, 1);
        INITIALIZEDREPO.showLogs();
    }

    /**Show the global log. Given ARGS.*/
    public static void showGlobal(String[] args) {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("global-log", args, 1);
        INITIALIZEDREPO.showGlobal();
    }

    /**find all the commits with the given commit log message. Given ARGS.*/
    public static void find(String[] args) {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("find", args, 2);
        INITIALIZEDREPO.findCommits(args[1]);
    }

    /**Print out the status. Given ARGS.*/
    public static void printStatus(String[] args) {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("status", args, 1);
        INITIALIZEDREPO.printStatus();
    }

    /**Do the checkout operation. Given ARGS. Given ARGS.*/
    public static void chekout(String[] args) throws IOException {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (args.length == 3 && args[1].equals("--")) {
            INITIALIZEDREPO.fileNameCheckOut(args[2]);
        }  else if (args.length == 4 && args[2].equals("--")) {
            if (args[1].length() < SHASIZE) {
                String id = findActualID(args[1]);
                if (id == null) {
                    System.out.println("No commit with that id exists.");
                    System.exit(0);
                }
                INITIALIZEDREPO.commitIDCheckout(id, args[3]);
            } else {
                INITIALIZEDREPO.commitIDCheckout(args[1], args[3]);
            }
        } else if (args.length == 2) {
            INITIALIZEDREPO.checkOutBranch(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /**Create a new branch. ARGS.*/
    public static void createNewBranch(String[] args) throws IOException {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("branch", args, 2);
        INITIALIZEDREPO.createNewBranch(args[1]);

    }

    /**Remove a branch. ARGS.*/
    public static void removeBranch(String[] args) {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("rm-branch", args, 2);
        INITIALIZEDREPO.removeBranch(args[1]);
    }

    /**Reset. Given ARGS.*/
    public static void reset(String[] args) throws IOException {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("reset", args, 2);
        if (args[1].length() < SHASIZE) {
            String id = findActualID(args[1]);
            if (id == null) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            INITIALIZEDREPO.reset(id);
        } else {
            INITIALIZEDREPO.reset(args[1]);
        }

    }

    /**Merge BRANCHNAME with the current head branch. Given ARGS.*/
    public static void merge(String[] args) throws IOException {
        if (!DOTFILE.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        validateNumArgs("merge", args, 2);
        INITIALIZEDREPO.merge(args[1]);
    }

    /** Check if ARGS has CMD in it and has N elements.*/
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            System.out.println("Invalid number of arguments.");
            System.exit(0);
        }
    }

    /**Given SHORTID, return the String of long id.*/
    public static String findActualID(String shortId) {
        for (String cmt: Utils.plainFilenamesIn(ALL_COMMITS)) {
            if (cmt.contains(shortId)) {
                return cmt;
            }
        }
        return null;
    }
}
