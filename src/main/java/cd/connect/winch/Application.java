package cd.connect.winch;

import cd.connect.winch.adaptors.HostedRepositoryAPI;
import cd.connect.winch.adaptors.RepositoryApiFactory;
import cd.connect.winch.model.PullRequest;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class Application {
    private static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        HostedRepositoryAPI repositoryAPI = RepositoryApiFactory.getRepositoryAPI(args[0]);
        Optional<PullRequest> firstPullRequest = repositoryAPI
                .getReadyPullRequests(args[0]).stream()
                .findFirst();

        firstPullRequest
                .ifPresent(pullRequest -> {
                    try {
                        Repository repository = new FileRepositoryBuilder()
                                .readEnvironment()
                                .findGitDir()
                                .build();
                        Git git = new Git(repository);
                        try {
                            git.checkout().setName(pullRequest.getBranch()).call();
                            RebaseResult result = git.rebase().setUpstream("origin/master").call();
                            log.info("Rebase has state: {}", result.getStatus());
                            if (result.getStatus().isSuccessful()) {
                                git.add().addFilepattern(".").call();
                                git.commit().setMessage("Rebased by Winch").call();

                                SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                                    @Override
                                    protected void configure(OpenSshConfig.Host hc, Session session) {
                                    }
                                };

                                git.push()
                                        .setTransportConfigCallback(transport -> ((SshTransport) transport).setSshSessionFactory(sshSessionFactory))
                                        .setForce(true)
                                        .add(pullRequest.getBranch()).call();
                            } else {
                                repositoryAPI.unapproveAndComment(pullRequest, "Cannot rebase, fix the branch manually please");
                            }
                        } catch (GitAPIException e) {
                            log.error("Failed to perform git operation", e);
                            throw new RuntimeException(e);
                        }
                    } catch (IOException e) {
                        log.error("Error processing PR", e);
                        throw new RuntimeException(e);
                    }
                });
    }
}