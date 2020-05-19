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

    private String projectKey;
    private String issueType;
    private Map<TrakrPriority, String> priorityMap;
    private Trakr.Type trackerName;
    private Trakr.Connection connection;
    private boolean dryRun;
    private boolean updateSummary;
    private boolean updateDescription;
    private boolean updateLabels;
    private boolean prioritizeUp;
    private boolean prioritizeDown;
    private String assignee;
    private HashMap<String, List<String>> transitions;
    private String reOpenStatus;
    private List<String> resolvedStatuses;
    private List<String> closedStatuses;
    private List<String> ignoreForLabels;
    private List<String> ignoreForStatuses;
    private Changes autoReopen;
    private Changes autoResolve;

    public StewardConfig(String projectKey, String issueType, Map<TrakrPriority, String> priorityMap,
                         Trakr.Type trackerName, Trakr.Connection connection) throws StewardException {
        this.projectKey = projectKey;
        this.issueType = issueType;
        this.priorityMap = priorityMap;
        this.trackerName = trackerName;
        this.connection = connection;
        this.dryRun = false;
        this.updateSummary = false;
        this.updateDescription = false;
        this.updateLabels = false;
        this.prioritizeUp = false;
        this.prioritizeDown = false;
        validate();
    }

    StewardConfig() {
        this.dryRun = false;
        this.updateSummary = false;
        this.updateDescription = false;
        this.updateLabels = false;
        this.prioritizeUp = false;
        this.prioritizeDown = false;
    }

    public static StewardConfig getConfig() {
        StewardConfig config = StewardConfigBuilder.buildConfig();
        if (config == null) {
            System.out.println("Please set the following environment variables.");
            System.out.println(StewardEnvar.getVarDefinitions());
        }
        return config;
    }

    void validate() throws StewardException {
        if (projectKey == null || projectKey.isEmpty()) {
            throw new StewardException("A valid project key is required");
        }
        if (issueType == null || issueType.isEmpty()) {
            throw new StewardException("A valid issue type is required");
        }
        if (priorityMap == null || priorityMap.isEmpty()) {
            throw new StewardException("A valid priority mapping is required");
        }
        if (trackerName == null) {
            throw new StewardException("A valid tracker name is required");
        }
        if (connection == null) {
            throw new StewardException("A valid credential is required");
        }
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

    Trakr.Type getTrackerName() {
        return trackerName;
    }

    public void setTrackerName(Trakr.Type trackerName) {
        this.trackerName = trackerName;
    }

    Trakr.Connection getConnection() {
        return connection;
    }

    public void setConnection(Trakr.Connection connection) {
        this.connection = connection;
    }

    boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    Map<TrakrPriority, String> getPriorityMap() {
        if (priorityMap == null) {
            priorityMap = new HashMap<>();
        }
        return priorityMap;
    }

    public void setPriorityMap(Map<TrakrPriority, String> priorityMap) {
        this.priorityMap = priorityMap;
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

    boolean isPrioritizeUp() {
        return this.prioritizeUp;
    }

    public void setPrioritizeUp(boolean prioritizeUp) {
        this.prioritizeUp = prioritizeUp;
    }

    boolean isPrioritizeDown() {
        return this.prioritizeDown;
    }

    public void setPrioritizeDown(boolean prioritizeDown) {
        this.prioritizeDown = prioritizeDown;
    }

    boolean isClosingAllowed() {
        return autoResolve != null && transitions != null &&
                (autoResolve.isTransition() || autoResolve.isComment());
    }

    boolean isOpeningAllowedForStatus(String status) {
        if (autoReopen != null && transitions != null &&
                (autoReopen.isTransition() || autoReopen.isComment())) {
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

    String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
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

    public void setIssueType(String issueType) {
        this.issueType = issueType;
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

        private boolean transition;
        private boolean comment;
        private int commentInterval;

        public Changes(boolean transition, boolean comment, int commentInterval) {
            this.transition = transition;
            this.comment = comment;
            if (commentInterval < 1) {
                this.commentInterval = defaultCommentInterval;
            } else {
                this.commentInterval = commentInterval;
            }
        }

        void setCommentInterval(int commentInterval) {
            this.commentInterval = commentInterval;
        }

        boolean isTransition() {
            return transition;
        }

        void setTransition(boolean transition) {
            this.transition = transition;
        }

        boolean isComment() {
            return comment;
        }

        void setComment(boolean comment) {
            this.comment = comment;
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
                return (lastComment == null) || (commentInterval > 0 &&
                        lastComment.getUpdatedDate().getTime() < commentBeforeTime);
            }
            return false;
        }

    }
}
