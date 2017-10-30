package cd.connect.winch.adaptors;

import cd.connect.winch.model.PullRequest;

import java.util.List;

public interface HostedRepositoryAPI {

    List<PullRequest> getReadyPullRequests(String repoName);

}
