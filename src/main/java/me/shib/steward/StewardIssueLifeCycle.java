package me.shib.steward;

import me.shib.lib.trakr.TrakrIssue;

import java.util.ArrayList;
import java.util.List;

public final class StewardIssueLifeCycle {

    private transient final TrakrIssue issue;
    private final boolean exists;
    private final List<Exception> exceptions;
    private boolean created;
    private boolean assigned;
    private boolean priorityUpdated;
    private boolean labelsUpdated;
    private boolean titleUpdated;
    private boolean descriptionUpdated;
    private boolean transitioned;
    private boolean resolved;
    private boolean reOpened;
    private boolean commented;
    private boolean ignored;

    StewardIssueLifeCycle(TrakrIssue issue, boolean exists) {
        this.issue = issue;
        this.exists = exists;
        this.exceptions = new ArrayList<>();
        this.created = false;
        this.assigned = false;
        this.priorityUpdated = false;
        this.labelsUpdated = false;
        this.titleUpdated = false;
        this.descriptionUpdated = false;
        this.transitioned = false;
        this.resolved = false;
        this.reOpened = false;
        this.commented = false;
        this.ignored = false;
    }

    void addException(Exception e) {
        this.exceptions.add(e);
    }

    void setCreated() {
        this.created = true;
    }

    public void setAssigned() {
        this.assigned = true;
    }

    void setPriorityUpdated() {
        this.priorityUpdated = true;
    }

    void setLabelsUpdated() {
        this.labelsUpdated = true;
    }

    void setTitleUpdated() {
        this.titleUpdated = true;
    }

    void setDescriptionUpdated() {
        this.descriptionUpdated = true;
    }

    void setResolved() {
        this.transitioned = true;
        this.resolved = true;
    }

    void setReOpened() {
        this.transitioned = true;
        this.reOpened = true;
    }

    void setCommented() {
        this.commented = true;
    }

    void setNotUpdated() {
        this.assigned = false;
        this.priorityUpdated = false;
        this.labelsUpdated = false;
        this.titleUpdated = false;
        this.descriptionUpdated = false;
        this.transitioned = false;
        this.commented = false;
    }

    void setIgnored() {
        this.ignored = true;
    }

    TrakrIssue getIssue() {
        return issue;
    }

    public boolean isExists() {
        return exists;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isUpdated() {
        return assigned || priorityUpdated || labelsUpdated || titleUpdated ||
                descriptionUpdated || transitioned || commented;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public boolean isPriorityUpdated() {
        return priorityUpdated;
    }

    public boolean isLabelsUpdated() {
        return labelsUpdated;
    }

    public boolean isTitleUpdated() {
        return titleUpdated;
    }

    public boolean isDescriptionUpdated() {
        return descriptionUpdated;
    }

    public boolean isTransitioned() {
        return transitioned;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean isReOpened() {
        return reOpened;
    }

    public boolean isCommented() {
        return commented;
    }

    public boolean isIgnored() {
        return ignored;
    }

}