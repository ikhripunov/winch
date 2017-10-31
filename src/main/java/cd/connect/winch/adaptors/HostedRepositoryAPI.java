package cd.connect.winch.adaptors;

import cd.connect.winch.model.PullRequest;

import java.io.IOException;
import java.util.List;

public interface HostedRepositoryAPI {

    List<PullRequest> getReadyPullRequests(String repoName);

    void unapproveAndComment(PullRequest pr, String comment) throws IOException;

}
