package cd.connect.winch.adaptors;

import cd.connect.winch.model.Comment;
import cd.connect.winch.model.PullRequest;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cd.connect.winch.model.PullRequest.fromGHPullRequest;

public class GitHubRepositoryApi implements HostedRepositoryAPI {
    private static final String MAGIC_WORD = "Winch deploy";
    private static final Pattern PRIORITY_REGEX = Pattern.compile(".*Winch priority (?<priority>\\d).*");
    private static final String MASTER_BRANCH = "master";
    private static final String PRIORITY = "priority";
    private Logger log = LoggerFactory.getLogger(GitHubRepositoryApi.class);

    @Override
    public List<PullRequest> getReadyPullRequests(String repoName) {
        try {
            GitHub github = GitHub.connectUsingOAuth("e54f6ac3c4867c8fc6b413032e938770fe3c796d");
            return github.getRepository(repoName).getPullRequests(GHIssueState.ALL).stream()
                    .filter(pr -> pr.getBase().getRef().equals(MASTER_BRANCH))
                    .filter(pr -> {
                        try {
                            return !pr.isMerged() && pr.getMergeable();
                        } catch (IOException e) {
                            log.error("Failed to get PR ", e);
                            throw new RuntimeException(e);
                        }
                    })
                    .map(pr -> {
                        try {
                            return fromGHPullRequest(pr);
                        } catch (IOException e) {
                            log.error("Failed to get PR [" + pr.getId() + "] comments", e);
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(pr -> pr.getComments().stream()
                            .anyMatch(comment -> comment.getBody().contains(MAGIC_WORD))
                    )
                    .sorted((pr1, pr2) -> {
                        Optional<Long> pr1Priority = getPriorityFromPR(pr1);
                        Optional<Long> pr2Priority = getPriorityFromPR(pr2);
                        return Long.compare(pr1Priority.orElse(pr1.getCreatedAt().getTime()), pr2Priority.orElse(pr2.getCreatedAt().getTime()));
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private Optional<Long> getPriorityFromPR(PullRequest pr) {
        return pr.getComments().stream()
                .sorted(Comparator.comparing(Comment::getLastUpdated).reversed())
                .map(comment -> PRIORITY_REGEX.matcher(comment.getBody()))
                .map(matcher -> matcher.matches() ? Long.parseLong(matcher.group(PRIORITY)) : 4L)
                .findFirst();
    }
}
