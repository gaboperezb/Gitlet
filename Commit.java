package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String author;
    private String parentId;
    private Instant date;
    private HashMap<String, String> files;
    private String id;
    private String secondParentId;


    public Commit(String message, String parentId, String secondParentId, HashMap<String, String> files) {
        this.message = message;
        this.author = "Gabriel";
        this.parentId = parentId;
        this.secondParentId = secondParentId;
        this.date = Instant.ofEpochMilli(new Date().getTime());
        this.files = files;


    }

    public Commit() {
       this.message = "initial commit";
       this.author = "Gabriel";
       this.parentId = null;
       this.secondParentId = null;
       this.date = Instant.ofEpochMilli(0);
       this.files = new HashMap<String, String>();
       this.id = sha1(this.message, this.author, this.date.toString());


    }

    public Commit cloneCommit(String message) {
        HashMap<String, String> newFiles = new HashMap<>();
        for (Map.Entry<String, String> set : this.getFiles().entrySet()) {
            newFiles.put(set.getKey(),set.getValue());
        }
        Commit newCommit = new Commit(message, this.getId(), null, newFiles);
        return newCommit;
    }

    public void setSha1Id() {
        ArrayList<String> mapping = new ArrayList<String>();
        for (Map.Entry<String, String> pair : this.files.entrySet()) {
            mapping.add(pair.getKey());
            mapping.add(pair.getValue());
        }

        String id = this.parentId == null ? "null" : this.parentId; //first commit
        mapping.add(this.message);
        mapping.add(this.author);
        mapping.add(id);
        mapping.add(this.date.toString());
        this.id = sha1(mapping.toArray());

    }
    public void saveCommit() {
        File f = join(Repository.COMMITS_DIR, this.getId());
        writeObject(f, this);
    }

    public static Commit getCurrentCommit() throws IOException {
        String head = Repository.getHeadRef();
        File f = join(Repository.COMMITS_DIR, head);
        Commit commit = readObject(f, Commit.class);
        return commit;
    }

    public static Commit getCommitById(String id) throws IOException {
        if (id == null) {
            return null;
        }
        File f = join(Repository.COMMITS_DIR, id);
        Commit commit = readObject(f, Commit.class);
        return commit;
    }

    public HashMap<String, String> getFiles() {
        return files;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public String getSecondParentId() {
        return secondParentId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getAuthor() {
        return author;
    }
    public Instant getDate() {
        return date;
    }

    public static void log() throws IOException {
        Commit commit = getCurrentCommit();
        log(commit);

    }

    private static void log(Commit commit) throws IOException {
        if(commit == null) {
            return;
        }
        commit.printInfo();
        Commit nextCommit = getCommitById(commit.parentId);
        log(nextCommit);
    }

    public static void globalLog() throws IOException {
        List<String> commitsNames = plainFilenamesIn(Repository.COMMITS_DIR);
        for (int i = 0; i < commitsNames.size(); i++) {
            File f = join(Repository.COMMITS_DIR, commitsNames.get(i));
            Commit commit = readObject(f, Commit.class);
            commit.printInfo();
        }

    }

    public static void find(String message) throws IOException {
        List<String> commitsNames = plainFilenamesIn(Repository.COMMITS_DIR);
        for (int i = 0; i < commitsNames.size(); i++) {
            File f = join(Repository.COMMITS_DIR, commitsNames.get(i));
            Commit commit = readObject(f, Commit.class);
            if(commit.message.equals(message)) {
                commit.printInfo();
            }
        }
    }

    public void printInfo() {
        System.out.println("===");
        System.out.println("commit " + this.id);
        System.out.println("Date " + this.date);
        System.out.println(this.message);
        System.out.println();
    }

}
