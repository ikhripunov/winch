package cd.connect.winch.adaptors;

import cd.connect.winch.adaptors.GitHubRepositoryApi;
import cd.connect.winch.adaptors.HostedRepositoryAPI;

public class RepositoryApiFactory {
    public static HostedRepositoryAPI getRepositoryAPI(String repositoryUrl) {
        return new GitHubRepositoryApi();
    }
}
