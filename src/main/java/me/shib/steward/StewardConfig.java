package me.shib.steward;

import me.shib.lib.trakr.*;

import java.util.*;

public final class StewardConfig {

    static transient final String issueFixedComment = "This issue has been fixed.";
    static transient final String issueNotFixedComment = "Found that the issue is still not fixed.";
    static transient final String resolveRequestComment = "Please resolve this issue.";
    static transient final String reopenRequestComment = "Please reopen this issue.";
    static transient final String autoResolvingNotificationComment = "Auto resolving this issue.";
    static transient final String closingNotificationComment = "Closing this issue after verification.";
    static transient final String reopeningNotificationComment = "Reopening this issue as it is not fixed.";

    private final String project;
    private final String issueType;
    private final Map<String, TrakrPriority> priorityMap;
    private final Trakr.Type trackerType;
    private final Trakr.Connection connection;
    private boolean readOnly;
    private boolean updateSummary;
    private boolean updateDescription;
    private boolean updateLabels;
    private boolean prioritize;
    private boolean dePrioritize;
    private String assignee;
    private HashMap<String, List<String>> transitions;
    private String reOpenStatus;
    private List<String> resolvedStatuses;
    private List<String> closedStatuses;
    private List<String> ignoreForLabels;
    private List<String> ignoreForStatuses;
    private Changes autoReopen;
    private Changes autoResolve;

    public StewardConfig(String project, String issueType, Map<String, TrakrPriority> priorityMap,
                         Trakr.Type trackerType, Trakr.Connection connection) {
        this.project = project;
        this.issueType = issueType;
        this.priorityMap = priorityMap;
        this.trackerType = trackerType;
        this.connection = connection;
        this.readOnly = false;
    }

    public void setTransitions(HashMap<String, List<String>> transitions) {
        this.transitions = transitions;
    }

    public void setReOpenStatus(String reOpenStatus) {
        this.reOpenStatus = reOpenStatus;
    }

    public void setResolvedStatuses(List<String> resolvedStatuses) {
        this.resolvedStatuses = resolvedStatuses;
    }

    public void setIgnoreForLabels(List<String> ignoreForLabels) {
        this.ignoreForLabels = ignoreForLabels;
    }

    public void setIgnoreForStatuses(List<String> ignoreForStatuses) {
        this.ignoreForStatuses = ignoreForStatuses;
    }

    Trakr.Type getTrackerType() {
        return trackerType;
    }

    Trakr.Connection getConnection() {
        return connection;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    Map<String, TrakrPriority> getPriorityMap() {
        return priorityMap;
    }

    Changes getAutoReopen() {
        return autoReopen;
    }

    public void setAutoReopen(Changes autoReopen) {
        this.autoReopen = autoReopen;
    }

    Changes getAutoResolve() {
        return autoResolve;
    }

    public void setAutoResolve(Changes autoResolve) {
        this.autoResolve = autoResolve;
    }

    boolean isResolvedStatus(String currentStatus) {
        for (String status : resolvedStatuses) {
            if (status.equalsIgnoreCase(currentStatus)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getTransitionPath(List<String> path, String fromStatus, List<String> toStatuses) {
        if (path.contains(fromStatus)) {
            return new ArrayList<>();
        }
        path.add(fromStatus);
        if (toStatuses.contains(fromStatus)) {
            return path;
        }
        List<List<String>> temp = new ArrayList<>();
        List<String> forwardStatuses = transitions.get(fromStatus);
        if (forwardStatuses == null) {
            System.out.println("Unable to find transitions for status: " + fromStatus);
        }
        for (String status : transitions.get(fromStatus)) {
            List<String> current = getTransitionPath(new ArrayList<>(path), status, toStatuses);
            if (current.size() > 0 && toStatuses.contains(current.get(current.size() - 1))) {
                temp.add(current);
            }
        }
        int minSize = 0;
        List<String> selected = null;
        for (List<String> list : temp) {
            if ((minSize == 0) || (list.size() > 0 && list.size() < selected.size())) {
                selected = list;
                minSize = selected.size();
            }
        }
        if (selected != null && selected.size() > 1) {
            return selected;
        }
        return path;
    }

    List<String> getTransitionsToOpen(String currentStatus) {
        List<String> reOpenStatuses = new ArrayList<>();
        reOpenStatuses.add(reOpenStatus);
        return getTransitionPath(new ArrayList<>(), currentStatus, reOpenStatuses);
    }

    List<String> getTransitionsToClose(String currentStatus) {
        return getTransitionPath(new ArrayList<>(), currentStatus, closedStatuses);
    }

    boolean isUpdateSummary() {
        return this.updateSummary;
    }

    public void setUpdateSummary(boolean updateSummary) {
        this.updateSummary = updateSummary;
    }

    boolean isUpdateDescription() {
        return this.updateDescription;
    }

    public void setUpdateDescription(boolean updateDescription) {
        this.updateDescription = updateDescription;
    }

    boolean isPrioritize() {
        return this.prioritize;
    }

    public void setPrioritize(boolean prioritize) {
        this.prioritize = prioritize;
    }

    boolean isDePrioritize() {
        return this.dePrioritize;
    }

    public void setDePrioritize(boolean dePrioritize) {
        this.dePrioritize = dePrioritize;
    }

    boolean isClosingAllowed() {
        return autoResolve != null && transitions != null &&
                (autoResolve.isMoveStatus() || autoResolve.isComment());
    }

    boolean isOpeningAllowedForStatus(String status) {
        if (autoReopen != null && transitions != null &&
                (autoReopen.isMoveStatus() || autoReopen.isComment())) {
            for (String s : resolvedStatuses) {
                if (s.equalsIgnoreCase(status)) {
                    return true;
                }
            }
            for (String s : closedStatuses) {
                if (s.equalsIgnoreCase(status)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isIssueIgnorable(TrakrIssue issue) {
        if (ignoreForStatuses != null) {
            for (String status : ignoreForStatuses) {
                if (status.equalsIgnoreCase(issue.getStatus())) {
                    return true;
                }
            }
        }
        if (ignoreForLabels != null) {
            for (String ignorableLabel : ignoreForLabels) {
                for (String issueLabel : issue.getLabels()) {
                    if (ignorableLabel.equalsIgnoreCase(issueLabel)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    String getProject() {
        return project;
    }

    String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    List<String> getClosedStatuses() {
        return closedStatuses;
    }

    public void setClosedStatuses(List<String> closedStatuses) {
        this.closedStatuses = closedStatuses;
    }

    public String getIssueType() {
        return issueType;
    }

    public boolean isUpdateLabels() {
        return updateLabels;
    }

    public void setUpdateLabels(boolean updateLabels) {
        this.updateLabels = updateLabels;
    }

    static class Changes {

        private transient static final int defaultCommentInterval = 30;
        private transient static final long oneDay = 86400000;

        private final boolean moveStatus;
        private final boolean comment;
        private final int commentInterval;

        public Changes(boolean moveStatus, boolean comment, int commentInterval) {
            this.moveStatus = moveStatus;
            this.comment = comment;
            if (commentInterval < 1) {
                this.commentInterval = defaultCommentInterval;
            } else {
                this.commentInterval = commentInterval;
            }
        }

        boolean isMoveStatus() {
            return moveStatus;
        }

        boolean isComment() {
            return comment;
        }

        boolean isCommentable(TrakrIssue issue, TrakrContent commentToAdd) throws TrakrException {
            if (comment) {
                TrakrComment lastComment = null;
                for (TrakrComment comment : issue.getComments()) {
                    if (comment.getBody().toLowerCase().contains(commentToAdd.getMarkdownContent().toLowerCase())) {
                        lastComment = comment;
                    }
                }
                long commentBeforeTime = new Date().getTime() - commentInterval * oneDay;
                return (lastComment == null) || (lastComment.getUpdatedDate().getTime() < commentBeforeTime);
            }
            return false;
        }

    }
}
