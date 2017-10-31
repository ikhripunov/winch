package cd.connect.winch.model;

import org.kohsuke.github.GHPullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PullRequest {
    private static Logger log = LoggerFactory.getLogger(PullRequest.class);
    private Integer id;
    private List<Comment> comments;
    private Date createdAt;
    private String head;
    private String branch;

    public Integer getId() {
        return id;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getBranch() {
        return branch;
    }

    public String getHead() {
        return head;
    }

    private PullRequest(Integer id, List<Comment> comments, Date createdAt, String head, String branch) {
        this.id = id;
        this.comments = comments;
        this.createdAt = createdAt;
        this.head = head;
        this.branch = branch;
    }

    public static PullRequest fromGHPullRequest(GHPullRequest pr) throws IOException {
        return new PullRequest(pr.getId(),
                pr.listComments().asList().stream()
                        .map(comment -> {
                            try {
                                return new Comment(comment.getBody(), comment.getUpdatedAt() == null ? comment.getCreatedAt() : comment.getUpdatedAt());
                            } catch (IOException e) {
                                log.error("Failed to get PR [" + pr.getId() + "] comments' details", e);
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList()),
                pr.getCreatedAt(),
                pr.getHead().getSha(),
                pr.getHead().getRef());
    }
}
