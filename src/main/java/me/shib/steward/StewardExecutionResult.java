package me.shib.steward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StewardExecutionResult {

    private final Map<String, StewardIssueLifeCycle> issueLifeCycles;
    private final List<Exception> exceptions;
    private int exists;
    private int created;
    private int updated;
    private int assigned;
    private int priorityUpdated;
    private int labelsUpdated;
    private int titleUpdated;
    private int descriptionUpdated;
    private int transitioned;
    private int resolved;
    private int reOpened;
    private int commented;
    private int ignored;

    StewardExecutionResult() {
        this.issueLifeCycles = new HashMap<>();
        this.exceptions = new ArrayList<>();
        this.created = 0;
        this.updated = 0;
        this.assigned = 0;
        this.priorityUpdated = 0;
        this.labelsUpdated = 0;
        this.titleUpdated = 0;
        this.descriptionUpdated = 0;
        this.transitioned = 0;
        this.resolved = 0;
        this.reOpened = 0;
        this.commented = 0;
        this.ignored = 0;
    }

    void addException(Exception e) {
        this.exceptions.add(e);
    }

    void addIssueLifeCycle(StewardIssueLifeCycle issueLifeCycle) {
        this.issueLifeCycles.put(issueLifeCycle.getIssue().getKey(), issueLifeCycle);
    }

    void summarizeCount() {
        for (StewardIssueLifeCycle lifeCycle : issueLifeCycles.values()) {
            if (lifeCycle.isCreated()) {
                created++;
            }
            if (lifeCycle.isUpdated()) {
                updated++;
            }
            if (lifeCycle.isAssigned()) {
                assigned++;
            }
            if (lifeCycle.isPriorityUpdated()) {
                priorityUpdated++;
            }
            if (lifeCycle.isLabelsUpdated()) {
                labelsUpdated++;
            }
            if (lifeCycle.isTitleUpdated()) {
                titleUpdated++;
            }
            if (lifeCycle.isDescriptionUpdated()) {
                descriptionUpdated++;
            }
            if (lifeCycle.isTransitioned()) {
                transitioned++;
            }
            if (lifeCycle.isResolved()) {
                resolved++;
            }
            if (lifeCycle.isReOpened()) {
                reOpened++;
            }
            if (lifeCycle.isCommented()) {
                commented++;
            }
            if (lifeCycle.isIgnored()) {
                ignored++;
            }
        }
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public Map<String, StewardIssueLifeCycle> getIssueLifeCycles() {
        return issueLifeCycles;
    }

    public int getExists() {
        return exists;
    }

    public int getCreated() {
        return created;
    }

    public int getUpdated() {
        return updated;
    }

    public int getAssigned() {
        return assigned;
    }

    public int getPriorityUpdated() {
        return priorityUpdated;
    }

    public int getLabelsUpdated() {
        return labelsUpdated;
    }

    public int getTitleUpdated() {
        return titleUpdated;
    }

    public int getDescriptionUpdated() {
        return descriptionUpdated;
    }

    public int getTransitioned() {
        return transitioned;
    }

    public int getResolved() {
        return resolved;
    }

    public int getReOpened() {
        return reOpened;
    }

    public int getCommented() {
        return commented;
    }

    public int getIgnored() {
        return ignored;
    }

    @Override
    public String toString() {
        summarizeCount();
        return "\nExecution Summary:" +
                "\nCreated: " + created +
                "\nUpdated: " + updated +
                "\nAssigned: " + assigned +
                "\nPriority Changed: " + priorityUpdated +
                "\nLabels Update: " + labelsUpdated +
                "\nTitle Updated: " + titleUpdated +
                "\nDescription Updated: " + descriptionUpdated +
                "\nTransitioned: " + transitioned +
                "\nResolved: " + resolved +
                "\nReopened: " + reOpened +
                "\nCommented: " + commented +
                "\nIgnored: " + ignored;
    }
}