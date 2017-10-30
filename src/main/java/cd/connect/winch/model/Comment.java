package cd.connect.winch.model;

import java.util.Date;

public class Comment {
    String body;
    Date lastUpdated;

    public Comment(String body, Date lastUpdated) {
        this.body = body;
        this.lastUpdated = lastUpdated;
    }

    public String getBody() {
        return body;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }
}
