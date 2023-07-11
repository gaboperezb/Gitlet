package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */

    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.init();
                break;
            case "add":
                String fileName = args[1];
                Repository.add(fileName);
                break;
            case "commit":
                String message = args[1];
                Repository.commit(message);
                break;
            case "rm":
                String fileNameRM = args[1];
                Repository.remove(fileNameRM);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                String messageFilter = args[1];
                Repository.find(messageFilter);
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkoutByFile(args[2]);
                }

                //java gitlet.Main checkout [commit id] -- [file name]
                if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkoutById(args[1], args[3]);
                }

                //java gitlet.Main checkout [branch name]
                if (args.length == 2) {
                    Repository.checkoutByBranchName(args[1]);
                }
                break;
            case "branch":
                String branchName = args[1];
                Repository.branch(branchName);
                break;
            case "rm-branch":
                String branchNameRM = args[1];
                Repository.removeBranch(branchNameRM);
                break;
            case "reset":
                String commitIdReset = args[1];
                Repository.reset(commitIdReset);
                break;
            case "merge":
                String branchName1 = args[1];
                Repository.merge(branchName1);
                break;
        }


    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
