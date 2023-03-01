package me.shib.steward;

import me.shib.lib.trakr.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class StewardConfig {

    static final String issueFixedComment = "This issue has been fixed.";
    static final String issueNotFixedComment = "Found that the issue is still not fixed.";
    static final String resolveRequestComment = "Please resolve this issue.";
    static final String reopenRequestComment = "Please reopen this issue.";
    static final String autoResolvingNotificationComment = "Auto resolving this issue.";
    static final String closingNotificationComment = "Closing this issue after verification.";
    static final String reopeningNotificationComment = "Reopening this issue as it is not fixed.";
    private static final String issueCompleteIgnoreLabelPrefix = "Ignore";
    private static final String issuePriorityIgnoreLabelPrefix = "IgnorePriority";
    private static final int specialConditionHashLength = 8;
    private String projectKey;
    private String issueType;
    private Map<TrakrPriority, String> priorityMap;
    private Trakr.Type trackerName;
    private Trakr.Connection connection;
    private boolean dryRun;
    private Integer exitCodeOnIssues;
    private Integer exitCodeOnNewIssues;
    private Integer exitCodeOnFailure;
    private boolean updateTitle;
    private boolean updateDescription;
    private boolean updateLabels;
    private boolean prioritizeUp;
    private boolean prioritizeDown;
    private String assignee;
    private HashMap<String, List<String>> workflow;
    private String reOpenStatus;
    private List<String> resolvedStatuses;
    private List<String> closedStatuses;
    private String specialConditionSecret;
    private List<String> ignoreForStatuses;
    private Changes autoReopen;
    private Changes autoResolve;
    private boolean findingsToIssuesSyncDisabled;

    public StewardConfig(String projectKey, String issueType, Map<TrakrPriority, String> priorityMap,
                         Trakr.Type trackerName, Trakr.Connection connection) throws StewardException {
        this();
        this.projectKey = projectKey;
        this.issueType = issueType;
        this.priorityMap = priorityMap;
        this.trackerName = trackerName;
        this.connection = connection;
        validate();
    }

    StewardConfig() {
        this.dryRun = false;
        this.updateTitle = false;
        this.updateDescription = false;
        this.updateLabels = false;
        this.prioritizeUp = false;
        this.prioritizeDown = false;
        this.findingsToIssuesSyncDisabled = false;
    }

    public static StewardConfig getConfig() {
        try {
            StewardConfig config = getConfig(StewardEnvar.STEWARD_CONFIG.getAsString());
            config.validate();
            return config;
        } catch (StewardException e) {
            e.printStackTrace();
            System.out.println("Please set the following environment variables.");
            System.out.println(StewardEnvar.getVarDefinitions());
        }
        return null;
    }

    public static StewardConfig getConfig(String configURI) {
        return StewardConfigBuilder.buildConfig(configURI);
    }

    private static String getHS256(String message, String secret) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] secretBytes = secret.getBytes();
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(secretBytes, algorithm));
        byte[] bytes = mac.doFinal(message.getBytes());
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0, v; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void validate() throws StewardException {
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

    public Integer getExitCodeOnIssues() {
        return exitCodeOnIssues;
    }

    public void setExitCodeOnIssues(Integer exitCodeOnIssues) {
        this.exitCodeOnIssues = exitCodeOnIssues;
    }

    Integer getExitCodeOnNewIssues() {
        return exitCodeOnNewIssues;
    }

    public void setExitCodeOnNewIssues(Integer exitCodeOnNewIssues) {
        this.exitCodeOnNewIssues = exitCodeOnNewIssues;
    }

    Integer getExitCodeOnFailure() {
        return exitCodeOnFailure;
    }

    public void setExitCodeOnFailure(Integer exitCodeOnFailure) {
        this.exitCodeOnFailure = exitCodeOnFailure;
    }

    public void setWorkflow(HashMap<String, List<String>> workflow) {
        this.workflow = workflow;
    }

    public void setReOpenStatus(String reOpenStatus) {
        this.reOpenStatus = reOpenStatus;
    }

    public void setResolvedStatuses(List<String> resolvedStatuses) {
        this.resolvedStatuses = resolvedStatuses;
    }

    public void setSpecialConditionSecret(String specialConditionSecret) {
        this.specialConditionSecret = specialConditionSecret;
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
        List<String> forwardStatuses = workflow.get(fromStatus);
        if (forwardStatuses == null) {
            System.out.println("Unable to find transitions for status: " + fromStatus);
        }
        List<String> to = workflow.get(fromStatus);
        if (to != null) {
            for (String status : to) {
                List<String> current = getTransitionPath(new ArrayList<>(path), status, toStatuses);
                if (current.size() > 0 && toStatuses.contains(current.get(current.size() - 1))) {
                    temp.add(current);
                }
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

    boolean isUpdateTitle() {
        return this.updateTitle;
    }

    public void setUpdateTitle(boolean updateTitle) {
        this.updateTitle = updateTitle;
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

    boolean isPriorityChangeIgnored(TrakrIssue issue) {
        return isIgnoreLabelExists(issue, issuePriorityIgnoreLabelPrefix);
    }

    boolean isAutoResolveAllowed() {
        return autoResolve != null && workflow != null;
    }

    boolean isAutoResolveAllowedForStatus(String status) {
        if (isStatusIgnorable(status) && (autoResolve == null || !autoResolve.isIncludeIgnored())) {
            return false;
        }
        if (autoResolve != null && workflow != null) {
            for (String s : resolvedStatuses) {
                if (s.equalsIgnoreCase(status)) {
                    return false;
                }
            }
            for (String s : closedStatuses) {
                if (s.equalsIgnoreCase(status)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isReOpeningAllowedForStatus(String status) {
        if (isStatusIgnorable(status) && (autoReopen == null || !autoReopen.isIncludeIgnored())) {
            return false;
        }
        if (autoReopen != null && workflow != null) {
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

    private boolean isStatusIgnorable(String issueStatus) {
        if (ignoreForStatuses != null) {
            for (String status : ignoreForStatuses) {
                if (status.equalsIgnoreCase(issueStatus)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isIgnoreLabelExists(TrakrIssue issue, String ignoreLabelPrefix) {
        ignoreLabelPrefix = ignoreLabelPrefix.toLowerCase() + "-";
        if (issue.getLabels() != null) {
            for (String issueLabel : issue.getLabels()) {
                if (specialConditionSecret != null && issueLabel.toLowerCase().startsWith(ignoreLabelPrefix)) {
                    String issueIgnoreHash = issueLabel.toLowerCase()
                            .replaceFirst(ignoreLabelPrefix, "");
                    if (issueIgnoreHash.length() >= specialConditionHashLength) {
                        try {
                            String calculatedHash = getHS256(issue.getKey(), specialConditionSecret);
                            if (calculatedHash.endsWith(issueIgnoreHash)) {
                                return true;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        return false;
    }

    boolean isIssueCompletelyIgnorable(TrakrIssue issue) {
        return isIgnoreLabelExists(issue, issueCompleteIgnoreLabelPrefix);
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

    boolean isFindingsToIssuesSyncDisabled() {
        return findingsToIssuesSyncDisabled;
    }

    public void setFindingsToIssuesSyncDisabled(boolean findingsToIssuesSyncDisabled) {
        this.findingsToIssuesSyncDisabled = findingsToIssuesSyncDisabled;
    }

    public static class Changes {

        private static final int defaultCommentInterval = 30;
        private static final long oneDay = 86400000;

        private int afterDays;
        private boolean includeIgnored;
        private boolean transition;
        private boolean comment;
        private int commentInterval;

        public Changes(int afterDays, boolean includeIgnored, boolean transition, boolean comment, int commentInterval) {
            this.afterDays = afterDays;
            this.includeIgnored = includeIgnored;
            this.transition = transition;
            this.comment = comment;
            if (commentInterval < 1) {
                this.commentInterval = defaultCommentInterval;
            } else {
                this.commentInterval = commentInterval;
            }
        }

        void setAfterDays(int afterDays) {
            this.afterDays = afterDays;
        }

        boolean isIncludeIgnored() {
            return includeIgnored;
        }

        void setIncludeIgnored(boolean includeIgnored) {
            this.includeIgnored = includeIgnored;
        }

        void setCommentInterval(int commentInterval) {
            this.commentInterval = commentInterval;
        }

        boolean isTransition(TrakrIssue issue) {
            return transition && new Date().getTime() > (issue.getCreatedDate().getTime() + afterDays * oneDay);
        }

        void setTransition(boolean transition) {
            this.transition = transition;
        }

        void setComment(boolean comment) {
            this.comment = comment;
        }

        boolean isCommentable(TrakrIssue issue, TrakrContent commentToAdd) throws TrakrException {
            if (comment && new Date().getTime() > (issue.getCreatedDate().getTime() + afterDays * oneDay)) {
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
