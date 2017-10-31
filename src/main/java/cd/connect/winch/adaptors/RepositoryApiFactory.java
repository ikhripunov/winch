package cd.connect.winch.adaptors;

public class RepositoryApiFactory {
    public static HostedRepositoryAPI getRepositoryAPI(String repositoryUrl) {
        return new GitHubRepositoryApi();
    }
}
