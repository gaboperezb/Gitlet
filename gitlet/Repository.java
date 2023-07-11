package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGING_ADD_DIR = join(CWD, ".gitlet", "staging", "add_stage");
    public static final File STAGING_RM_DIR = join(CWD, ".gitlet", "staging", "rm_stage");
    public static final File COMMITS_DIR = join(CWD, ".gitlet", "commits");
    public static final File BLOBS_DIR = join(CWD, ".gitlet", "blobs");
    public static final File BRANCHES_DIR = join(CWD, ".gitlet", "branches");

    /* TODO: fill in the rest of this class. */

    public static void setupHead(String branch) throws IOException {

        File headFile = join(CWD,".gitlet", "HEAD");
        String headContents = readContentsAsString(headFile);
        writeContents(headFile, branch);

    }

    public static String getHeadRef() throws IOException {

        File headFile = join(CWD,".gitlet", "HEAD");
        String headContents = readContentsAsString(headFile);
        File currentBranchFile = join(BRANCHES_DIR,headContents);
        return readContentsAsString(currentBranchFile);

    }

    public static String getCurrentBranch() throws IOException {

        File headFile = join(CWD,".gitlet", "HEAD");
        String headContents = readContentsAsString(headFile);
        return headContents;

    }

    public static String getBranchPointer(String branch) throws IOException {
        File branchFile = join(BRANCHES_DIR,branch);
        String branchContents = readContentsAsString(branchFile);
        return branchContents;

    }

    public static List<String> getBranches() throws IOException {
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        return branches;
    }

    public static void updateBranch(String branch, String commit) throws IOException {

        File branchFile = join(BRANCHES_DIR, branch);
        if (!branchFile.exists()) {
            branchFile.createNewFile();
        }
        writeContents(branchFile, commit);

    }

    public static void init() throws IOException {

        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }

        if (!COMMITS_DIR.exists()) {
            COMMITS_DIR.mkdirs();
        }

        if (!STAGING_ADD_DIR.exists()) {
            STAGING_ADD_DIR.mkdirs();
        }

        if (!STAGING_RM_DIR.exists()) {
            STAGING_RM_DIR.mkdirs();
        }

        if (!BRANCHES_DIR.exists()) {
            BRANCHES_DIR.mkdirs();
        }

        if (!BLOBS_DIR.exists()) {
            BLOBS_DIR.mkdirs();
        }

        File f = join(CWD, ".gitlet", "HEAD");
        if (!f.exists()) {
            f.createNewFile();
        }

        Commit commit = new Commit();
        setupHead("master");
        updateBranch("master", commit.getId());
        commit.saveCommit();

    }

    public static void add(String fileName) throws IOException {

        File f = join(CWD, fileName);
        if(!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        File addFile = join(STAGING_ADD_DIR, "add");
        HashMap<String, String> addMap;
        Commit commit = Commit.getCurrentCommit();
        String currentVersion = commit.getFiles().get(fileName);

        if (addFile.exists()) {
            addMap = readObject(addFile, HashMap.class);
        } else {
            addMap = new HashMap<>();
        }

        /* Read contents of fileName as byteArray, to save them in the blobs folder sha-1d */
        byte[] contents = readContents(f);
        String sha1blob = sha1(contents);

        /* Save blob */
        File blobObject = join(BLOBS_DIR, sha1blob);
        writeContents(blobObject, contents);

        if (currentVersion != sha1blob) {
            addMap.put(fileName, sha1blob);
            writeObject(addFile, addMap);
        } else {
            addMap.remove(fileName);
            if (addMap.isEmpty()) {
               addFile.delete();
            } else {
                writeObject(addFile, addMap);
            }

        }

        //The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.

        File rmFile = join(STAGING_RM_DIR, "rm");
        if (rmFile.exists()) {
            HashMap<String, String> rmMap = readObject(rmFile, HashMap.class);
            rmMap.remove(fileName);
            if (rmMap.isEmpty()) {
                rmFile.delete();
            }
        }

    }

    public static void remove(String fileName) throws IOException {

        //Unstage the file if it is currently staged for addition.

        File addFile = join(STAGING_ADD_DIR, "add");
        HashMap<String, String> addMap;

        if (addFile.exists()) {
            addMap = readObject(addFile, HashMap.class);
            addMap.remove(fileName);
            writeObject(addFile, addMap);
            if (addMap.isEmpty()) {
                addFile.delete();
            }

        }

        //If the file is tracked in the current commit, stage it for removal and remove the file from the working
        // directory if the user has not already done so (do not remove it unless it is tracked in th
        // e current commit).

        Commit commit = Commit.getCurrentCommit();
        String trackedFile = commit.getFiles().get(fileName);

        if (trackedFile != null) {
            File f = join(CWD, fileName);
            File rmFile = join(STAGING_RM_DIR, "rm");
            HashMap<String, String> rmMap;

            if (rmFile.exists()) {
                rmMap = readObject(rmFile, HashMap.class);
            } else {
                rmMap = new HashMap<>();
            }
            rmMap.put(fileName, trackedFile);
            writeObject(rmFile, rmMap);
            f.delete();
        }


    }

    public static void commit(String message) throws IOException {

        File addFile = join(STAGING_ADD_DIR, "add");
        File rmFile = join(STAGING_RM_DIR, "rm");

        if (!addFile.exists() && !rmFile.exists()) {
            System.out.println("Nothing to commit");
            System.exit(0);
        }

        Commit currentCommit = Commit.getCurrentCommit();
        Commit newCommit = currentCommit.cloneCommit(message);

        if (addFile.exists()) {
            HashMap<String, String> addMap = readObject(addFile, HashMap.class);
            //add staging files
            for (Map.Entry<String, String> set : addMap.entrySet()) {
                newCommit.getFiles().put(set.getKey(), set.getValue());
            }
            addFile.delete();
        }

        //Files tracked in the current commit may be untracked in the new commit as a
        // result being staged for removal by the rm command (below).

        if (rmFile.exists()) {
            HashMap<String, String> rmMap = readObject(rmFile, HashMap.class);
            //remove staging files
            for (Map.Entry<String, String> set : rmMap.entrySet()) {
                newCommit.getFiles().remove(set.getKey(), set.getValue());
            }

            rmFile.delete();
        }

        newCommit.setSha1Id();
        newCommit.saveCommit();
        updateBranch(getCurrentBranch(), newCommit.getId());

    }

    public static void log() throws IOException {
        Commit.log();
    }


    public static void globalLog() throws IOException {
        Commit.globalLog();
    }

    public static void find(String message) throws IOException {
        Commit.find(message);
    }

    public static void status() throws IOException {

        File addFile = join(STAGING_ADD_DIR, "add");
        HashMap<String, String> addMap;
        Set<String> addStagedKeys = new HashSet<>();

        File rmFile = join(STAGING_RM_DIR, "rm");
        HashMap<String, String> rmMap;
        Set<String> rmStagedKeys = new HashSet<>();

        if (addFile.exists()) {
            addMap = readObject(addFile, HashMap.class);
            addStagedKeys = addMap.keySet();
        }

        if (rmFile.exists()) {
            rmMap = readObject(rmFile, HashMap.class);
            rmStagedKeys = rmMap.keySet();
            System.out.println(rmStagedKeys.isEmpty());
        }

        printStatus(getCurrentBranch(), getBranches(), addStagedKeys, rmStagedKeys);
    }

    public static void checkoutByFile(String fileName) throws IOException {
        Commit currentCommit = Commit.getCurrentCommit();
        String blob = currentCommit.getFiles().get(fileName);
        writeContentGit(blob, fileName);
    }

    public static void checkoutById(String commitId, String fileName) throws IOException {
        Commit commitById = Commit.getCommitById(commitId);

        if (commitById == null) {
            System.out.println("Wrong commit id.");
            System.exit(0);
        }

        String blob = commitById.getFiles().get(fileName);

        if (blob == null) {
            System.out.println("Wrong file name.");
            System.exit(0);
        }

        writeContentGit(blob, fileName);
    }
    public static void writeContentGit(String blob, String fileName) {
        File f = join(BLOBS_DIR, blob);
        File f1 = join(CWD, fileName);
        byte[] contents = readContents(f);
        writeContents(f1, contents);
    }

    public static void checkoutByBranchName(String branch) throws IOException {

        if (getCurrentBranch().equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        String branchPointer = getBranchPointer(branch);
        Commit commitById = Commit.getCommitById(branchPointer); //commit that checked-out branch points at
        Commit currentCommit = Commit.getCurrentCommit(); //commit that checked-out branch points at

        deleteContentsGit(currentCommit.getFiles().keySet(), commitById);

        //Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
        for (String key : currentCommit.getFiles().keySet()) {
            if (!commitById.getFiles().containsKey(key)) {
                File f = join(CWD, key);
                f.delete();
            }
        }

        //Takes all files in the commit at the head of the given branch, and puts them in the working directory,
        // overwriting the versions of the files that are already there if they exist.
        writeContentsGit(commitById);

        deleteStagingArea();
        setupHead(branch);

    }

    public static void branch(String branchName) throws IOException {
        updateBranch(branchName, getHeadRef());
    }


    public static void removeBranch(String branchName) throws IOException {
        if(branchName.equals(getCurrentBranch())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        File f = join(BRANCHES_DIR, branchName);
        if(f.exists()) {
            f.delete();
        }

    }

    public static void writeContentsGit(Commit commit) {
        for (Map.Entry<String, String> set : commit.getFiles().entrySet()) {
            File f = join(BLOBS_DIR, set.getValue());
            File f1 = join(CWD, set.getKey());
            byte[] contents = readContents(f);
            writeContents(f1, contents);
        }
    }

    public static void deleteContentsGit(Collection<String> list, Commit commit) {
        for (String key : list) {
            if (!commit.getFiles().containsKey(key)) {
                File f = join(CWD, key);
                f.delete();
            }
        }
    }


    public static void reset(String commitId) throws IOException {

        File cwdf = join(CWD);
        Commit commitById = Commit.getCommitById(commitId);
        List<String> cwdFiles = plainFilenamesIn(cwdf);


        //Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
        deleteContentsGit(cwdFiles, commitById);
        updateBranch(getCurrentBranch(), commitId);
        deleteStagingArea();
        writeContentsGit(commitById);

    }

    public static void merge(String branchName) throws IOException {

        File files = join(COMMITS_DIR);
        List<String> nodes = plainFilenamesIn(files);
        CommitsGraph graph = new CommitsGraph(nodes.size());
        Map<String, Integer> nodesMap = new HashMap<>();
        String firstCommitId = "first"; //placeholder

        for (int i = 0; i < nodes.size(); i++) {
            nodesMap.put(nodes.get(i), i);
        }

        //build graph
        for (int i = 0; i < nodes.size(); i++) {
            String id = nodes.get(i);
            Commit commit = Commit.getCommitById(id);
            String parentId = commit.getParentId();
            String secondParentId = commit.getSecondParentId();

            if(secondParentId != null) {
                int edgeTo = nodes.indexOf(secondParentId);
                graph.addEdge(i, edgeTo);
            }

            if(parentId != null) {
                int edgeTo = nodes.indexOf(parentId);
                graph.addEdge(i, edgeTo);
            } else {
                firstCommitId = commit.getId();
            }
        }

        String currentBranch = getHeadRef();
        int currentNode = nodesMap.get(currentBranch);
        DFS currentDFS = new DFS(graph, currentNode);

        int firstNode = nodesMap.get(firstCommitId);
        ArrayList<Integer> path = currentDFS.pathTo(firstNode);

        String checkedOutBranch = getBranchPointer(branchName);
        int checkedNode = nodesMap.get(checkedOutBranch);
        DFS checkedDFS = new DFS(graph, checkedNode);
        ArrayList<Integer> pathChecked = checkedDFS.pathTo(firstNode);

        int splitNode = DFS.splitNode(path, pathChecked);
        String splitId = nodes.get(splitNode);
        mergeDecision(splitId, checkedOutBranch);


    }

    public static void mergeDecision(String splitId, String checkedId) throws IOException {


        File addFile = join(STAGING_ADD_DIR, "add");
        HashMap<String, String> addMap;

        if (addFile.exists()) {
            addMap = readObject(addFile, HashMap.class);
        } else {
            addMap = new HashMap<>();
        }


        Commit currentCommit = Commit.getCurrentCommit();
        Commit checkedCommit = Commit.getCommitById(checkedId);
        Commit splitCommit = Commit.getCommitById(splitId);

        HashMap<String, String> currentCommitFiles = currentCommit.getFiles();
        HashMap<String, String> checkedCommitFiles = checkedCommit.getFiles();
        HashMap<String, String> splitCommitFiles = splitCommit.getFiles();

        for (Map.Entry<String, String> files : checkedCommitFiles.entrySet()) {
            String checkedValue = files.getValue();
            String checkedKey = files.getKey();
            String splitValue =  splitCommitFiles.get(checkedKey);
            String currentValue = currentCommitFiles.get(checkedKey);

            if(splitValue == currentValue && (checkedValue != currentValue)) {
                addMap.put(checkedKey, checkedValue);
            }

            ///... 7 more steps

            // add to staging area

            ///... new commit with second parent id

        }

    }

    public static void deleteStagingArea() {
        File addFile = join(STAGING_ADD_DIR, "add");
        addFile.delete();

        File rmFile = join(STAGING_RM_DIR, "rm");
        rmFile.delete();

    }

    public static void printStatus(String currentBranch, List<String> branches,
                                   Set<String> addedFiles, Set<String> removedFiles) {

        System.out.println("=== Branches ===");
        for (String branch:
                branches) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String fileName:
                addedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String fileName:
                removedFiles) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println();
    }
}
