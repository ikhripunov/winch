package cd.connect.winch;

import cd.connect.winch.adaptors.RepositoryApiFactory;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

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
                if (result.getStatus().isSuccessful() || result.getStatus().equals(RebaseResult.Status.UNCOMMITTED_CHANGES)) {
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage("Rebased by Winch").call();

                    SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                        @Override
                        protected void configure(OpenSshConfig.Host hc, Session session) {}

                        @Override
                        protected JSch createDefaultJSch(FS fs) throws JSchException {
                            JSch defaultJSch = super.createDefaultJSch(fs);
                            defaultJSch.addIdentity("/home/ikhripunov/clearpoint/connectwinch/id_rsa");
                            return defaultJSch;
                        }
                    };

                    git.push()
                            .setTransportConfigCallback(transport -> ((SshTransport)transport).setSshSessionFactory(sshSessionFactory))
                            .setForce(true)
                            .add(args[1]).call();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}