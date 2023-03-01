package me.shib.steward;


import me.shib.lib.trakr.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

final class DummyIssue extends TrakrIssue {

    private static final Random rand = new Random();

    private transient String projectKey;
    private transient String key;
    private transient String title;
    private transient String description;
    private transient String type;
    private transient String status;
    private transient TrakrPriority priority;
    private transient Date date;
    private transient TrakrIssue realIssue;
    private transient TrakrIssueBuilder creator;

    DummyIssue(Trakr trakr, TrakrIssueBuilder creator) {
        super(trakr);
        this.creator = creator;
        init(trakr);
    }

    DummyIssue(Trakr trakr, TrakrIssue issue) {
        super(trakr);
        this.realIssue = issue;
        init(trakr);
    }

    private void init(Trakr trakr) {
        this.date = new Date();
        if (creator != null) {
            this.projectKey = "DUMMY-" + creator.getProject();
            this.key = projectKey + "-" + rand.nextInt(1000000) + 1000000;
            this.title = creator.getTitle();
            this.description = creator.getDescription().getContent(trakr.getContentType());
            this.type = creator.getIssueType();
            this.status = creator.getStatus();
            this.priority = creator.getPriority();
        } else {
            this.projectKey = "DUMMY-" + realIssue.getProjectKey();
            this.key = "DUMMY-" + realIssue.getKey();
            this.title = realIssue.getTitle();
            this.description = realIssue.getDescription();
            this.type = realIssue.getType();
            this.status = realIssue.getStatus();
            this.priority = realIssue.getPriority();
        }
    }

    @Override
    public void refresh() throws TrakrException {
        if (realIssue != null) {
            realIssue.refresh();
        }
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getProjectKey() {
        return projectKey;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public TrakrPriority getPriority() {
        return priority;
    }

    @Override
    public Date getCreatedDate() {
        if (null != realIssue) {
            return realIssue.getCreatedDate();
        }
        return date;
    }

    @Override
    public Date getUpdatedDate() {
        if (null != realIssue) {
            return realIssue.getUpdatedDate();
        }
        return date;
    }

    @Override
    public Date getDueDate() {
        if (null != realIssue) {
            return realIssue.getDueDate();
        }
        return date;
    }

    private TrakrUser getDummyUser() {
        return new TrakrUser() {
            @Override
            public String getName() {
                return "Dummy Dude";
            }

            @Override
            public String getUsername() {
                return "dummy.dude";
            }

            @Override
            public String getEmail() {
                return "dude@dummy.com";
            }
        };
    }

    @Override
    public TrakrUser getReporter() {
        if (null != realIssue) {
            return realIssue.getReporter();
        }
        return getDummyUser();
    }

    @Override
    public TrakrUser getAssignee() {
        if (null != realIssue) {
            return realIssue.getAssignee();
        }
        return getDummyUser();
    }

    @Override
    public List<TrakrUser> getSubscribers() {
        if (null != realIssue) {
            return realIssue.getSubscribers();
        }
        List<TrakrUser> users = new ArrayList<>();
        users.add(getDummyUser());
        return users;
    }

    @Override
    public List<String> getLabels() {
        if (null != realIssue) {
            return realIssue.getLabels();
        }
        return new ArrayList<>();
    }

    @Override
    public Object getCustomField(String identifier) {
        if (null != realIssue) {
            return realIssue.getCustomField(identifier);
        }
        return null;
    }

    @Override
    public List<TrakrComment> getComments() throws TrakrException {
        if (null != realIssue) {
            return realIssue.getComments();
        }
        return new ArrayList<>();
    }

    @Override
    public TrakrComment addComment(TrakrContent comment) {
        return new DummyComment(comment, trakr.getContentType());
    }

    static final class DummyComment implements TrakrComment {

        private final Date date;
        private final String comment;

        DummyComment(TrakrContent comment, TrakrContent.Type type) {
            this.date = new Date();
            this.comment = comment.getContent(type);
        }

        @Override
        public String getBody() {
            return comment;
        }

        @Override
        public Date getCreatedDate() {
            return date;
        }

        @Override
        public Date getUpdatedDate() {
            return date;
        }
    }

}
