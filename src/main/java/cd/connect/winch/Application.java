package cd.connect.winch;

import cd.connect.winch.adaptors.RepositoryApiFactory;

public class Application {
    public static void main(String[] args) {
        System.out.println("tick-tick-tick-tick... starting winch...");
        RepositoryApiFactory.getRepositoryAPI(args[0])
                .getReadyPullRequests(args[0]).stream()
                .findFirst()
                .ifPresent(pullRequest -> {
                    System.out.println(pullRequest.getHeadSHA());
                    System.out.println(pullRequest.getBranch());
                });
    }
}