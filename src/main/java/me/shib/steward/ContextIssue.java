package me.shib.steward;

import me.shib.lib.trakr.*;

import java.util.Date;
import java.util.List;

final class ContextIssue extends TrakrIssue {

    private final transient ContextTrakr trakr;
    private final transient TrakrIssue issue;

    ContextIssue(ContextTrakr trakr, TrakrIssue issue) {
        super(trakr);
        this.trakr = trakr;
        this.issue = issue;
    }

    @Override
    public void refresh() throws TrakrException {
        issue.refresh();
        trakr.addToContext(this);
    }

    @Override
    public String getKey() {
        return issue.getKey();
    }

    @Override
    public String getProjectKey() {
        return issue.getProjectKey();
    }

    @Override
    public String getTitle() {
        return issue.getTitle();
    }

    @Override
    public String getDescription() {
        return issue.getDescription();
    }

    @Override
    public String getType() {
        return issue.getType();
    }

    @Override
    public String getStatus() {
        return issue.getStatus();
    }

    @Override
    public TrakrPriority getPriority() {
        return issue.getPriority();
    }

    @Override
    public Date getCreatedDate() {
        return issue.getCreatedDate();
    }

    @Override
    public Date getUpdatedDate() {
        return issue.getUpdatedDate();
    }

    @Override
    public Date getDueDate() {
        return issue.getDueDate();
    }

    @Override
    public TrakrUser getReporter() {
        return issue.getReporter();
    }

    @Override
    public TrakrUser getAssignee() {
        return issue.getAssignee();
    }

    @Override
    public List<TrakrUser> getSubscribers() {
        return issue.getSubscribers();
    }

    @Override
    public List<String> getLabels() {
        return issue.getLabels();
    }

    @Override
    public Object getCustomField(String identifier) {
        return issue.getCustomField(identifier);
    }

    @Override
    public List<TrakrComment> getComments() throws TrakrException {
        return issue.getComments();
    }

    @Override
    public TrakrComment addComment(TrakrContent comment) throws TrakrException {
        return issue.addComment(comment);
    }
}
