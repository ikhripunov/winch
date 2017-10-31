package cd.connect.winch;

import cd.connect.winch.adaptors.RepositoryApiFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;

public class Application {
    public static void main(String[] args) {
        System.out.println("tick-tick-tick-tick... starting winch...");
        RepositoryApiFactory.getRepositoryAPI(args[0])
                .getReadyPullRequests(args[0]).stream()
                .findFirst()
                .ifPresent(pullRequest -> {
                    System.out.println(pullRequest.getHead());
                    System.out.println(pullRequest.getBranch());
                });

        try {
            Repository repository = new FileRepositoryBuilder()
                    .readEnvironment()
                    .findGitDir()
                    .build();
            try {
                Git git = new Git(repository);
                git.checkout().setName(args[1]).call();
                RebaseResult result = git.rebase().setUpstream("origin/master").call();
                System.out.println("Rebase had state: " + result.getStatus() + ": " + result.getConflicts());
                if (result.getStatus().isSuccessful()) {
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage("Rebased by Winch").call();
                    git.push().add(args[1]).call();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}