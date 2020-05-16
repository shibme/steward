package me.shib.steward;

import me.shib.lib.trakr.*;

import java.util.*;

public final class Steward {

    private transient final StewardProcess stewardProcess;

    private final StewardConfig config;
    private final Trakr tracker;
    private final StewardData data;

    private Steward(StewardData data, StewardConfig config) throws TrakrException {
        this.stewardProcess = new StewardProcess();
        this.data = data;
        this.config = config;
        this.tracker = getContextTracker();
    }

    public static StewardProcess process(StewardData data, StewardConfig config) throws TrakrException {
        System.out.println("Findings Identified in " + data.getProject() + " [" + data.getConnector() + "]: " +
                data.getFindings().size());
        Steward steward = new Steward(data, config);
        steward.processFindings();
        steward.verifyExistingNonClosedIssues();
        return steward.stewardProcess;
    }

    private Trakr getContextTracker() throws TrakrException {
        TrakrQuery query = new TrakrQuery();
        query.add(TrakrQuery.Condition.project, TrakrQuery.Operator.matching, config.getProject());
        query.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getProject());
        query.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getConnector());
        for (String key : data.getContexts()) {
            query.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, key);
        }
        Trakr trakr = Trakr.getTrakr(config.getTrackerType(), config.getConnection(),
                config.getPriorityMap());
        trakr = new ContextTrakr(trakr, query, config.getConnection(), config.getPriorityMap());
        if (config.isReadOnly()) {
            trakr = new DummyTrakr(trakr, config.getConnection(), config.getPriorityMap());
        }
        return trakr;
    }

    private void createTrakrIssueForBug(StewardFinding finding) throws TrakrException {
        List<String> labels = new ArrayList<>();
        labels.add(data.getProject());
        labels.add(data.getConnector());
        labels.addAll(data.getContexts());
        labels.addAll(data.getTags());
        labels.addAll(finding.getContexts());
        TrakrIssueBuilder issueBuilder = new TrakrIssueBuilder();
        issueBuilder.setProject(config.getProject());
        issueBuilder.setTitle(finding.getTitle());
        issueBuilder.setIssueType(config.getIssueType());
        issueBuilder.setAssignee(config.getAssignee());
        issueBuilder.setPriority(finding.getPriority());
        issueBuilder.setDescription(new TrakrContent(finding.getDescription()));
        issueBuilder.setLabels(labels);
        TrakrIssue trackerIssue = tracker.createIssue(issueBuilder);
        System.out.println("Created new issue: " + trackerIssue.getKey() + " - " + trackerIssue.getTitle() + " with priority "
                + trackerIssue.getPriority());
    }

    private boolean isLabelExsitingInSet(Set<String> fromIssue, String labelForAvailabilityCheck) {
        for (String label : fromIssue) {
            if (label.equalsIgnoreCase(labelForAvailabilityCheck)) {
                return true;
            }
        }
        return false;
    }

    private void updateTrakrIssueForBug(TrakrIssue stewardIssue, StewardFinding stewardFinding) throws TrakrException {
        if (config.isIssueIgnorable(stewardIssue)) {
            System.out.println("Ignoring the issue: " + stewardIssue.getKey());
        }
        boolean issueUpdated = false;
        TrakrIssueBuilder issueBuilder = new TrakrIssueBuilder();
        issueBuilder.setProject(config.getProject());
        if (stewardIssue.getAssignee() == null && config.getAssignee() != null) {
            issueBuilder.setAssignee(config.getAssignee());
            issueUpdated = true;
        }
        if (config.isUpdateSummary() && !stewardIssue.getTitle().contentEquals(stewardFinding.getTitle())) {
            issueBuilder.setTitle(stewardFinding.getTitle());
            issueUpdated = true;
        }
        if (config.isUpdateDescription() &&
                !tracker.areContentsMatching(new TrakrContent(stewardFinding.getDescription()),
                        stewardIssue.getDescription())) {
            issueBuilder.setDescription(new TrakrContent(stewardFinding.getDescription()));
            issueUpdated = true;
        }
        if (config.isUpdateLabels()) {
            Set<String> updateSet = new HashSet<>(stewardIssue.getLabels());
            for (String labelFromBug : stewardFinding.getContexts()) {
                if (!isLabelExsitingInSet(updateSet, labelFromBug)) {
                    updateSet.add(labelFromBug);
                }
            }
            if (updateSet.size() != stewardIssue.getLabels().size()) {
                issueBuilder.setLabels(new ArrayList<>(updateSet));
                issueUpdated = true;
            }
        }
        StringBuilder comment = new StringBuilder();
        if (((stewardIssue.getPriority().getRank() < stewardFinding.getPriority().getRank()) && (config.isPrioritize()))
                || ((stewardIssue.getPriority().getRank() > stewardFinding.getPriority().getRank()) && (config.isDePrioritize()))) {
            issueBuilder.setPriority(stewardFinding.getPriority());
            System.out.println("Prioritizing " + stewardIssue.getKey() + " to " + tracker.getPriorityName(stewardFinding.getPriority()) + " based on actual priority.");
            comment.append("Prioritizing to **").append(tracker.getPriorityName(stewardFinding.getPriority())).append("** based on actual priority.");
            issueUpdated = true;
        }
        if (issueUpdated) {
            stewardIssue = tracker.updateIssue(stewardIssue, issueBuilder);
            if (!comment.toString().isEmpty()) {
                stewardIssue.addComment(new TrakrContent(comment.toString()));
            }
        }
        if (config.isOpeningAllowedForStatus(stewardIssue.getStatus())) {
            reopenIssue(stewardIssue);
        } else if (issueUpdated) {
            System.out.println("Updated the issue: " + stewardIssue.getKey() + " - "
                    + stewardIssue.getTitle());
        } else {
            System.out.println("Issue up-to date: " + stewardIssue.getKey() + " - "
                    + stewardIssue.getTitle());
        }
    }

    private List<String> toLowerCaseList(Collection<String> list) {
        List<String> lowerCaseList = new ArrayList<>();
        for (String item : list) {
            lowerCaseList.add(item.toLowerCase());
        }
        return lowerCaseList;
    }

    private boolean isVulnerabilityExists(TrakrIssue issue, List<StewardFinding> findings) {
        for (StewardFinding finding : findings) {
            if (toLowerCaseList(issue.getLabels()).containsAll(toLowerCaseList(finding.getContexts()))) {
                return true;
            }
        }
        return false;
    }

    private boolean closeIssue(TrakrIssue issue) throws TrakrException {
        if (config.isIssueIgnorable(issue)) {
            System.out.println("Ignoring the issue: " + issue.getKey());
            return false;
        }
        System.out.println("Issue: " + issue.getKey() + " has been fixed.");
        boolean transitioned = false;
        String originalStatus = issue.getStatus();
        if (config.getAutoResolve().isMoveStatus()) {
            List<String> transitions = config.getTransitionsToClose(issue.getStatus());
            System.out.println("Closing the issue " + issue.getKey() + ".");
            transitioned = transitionIssue(transitions, issue);
            if (!transitioned) {
                System.out.println("No path defined to Close the issue from \"" + issue.getStatus() + "\" state.");
            }
        }
        StringBuilder comment = new StringBuilder();
        if (config.getAutoResolve().isCommentable(issue, new TrakrContent(StewardConfig.issueFixedComment))) {
            comment.append("\n").append(StewardConfig.issueFixedComment);
            if (!transitioned) {
                comment.append("\n").append(StewardConfig.resolveRequestComment);
            }
        }
        if (transitioned) {
            if (!comment.toString().isEmpty()) {
                comment.append("\n");
            }
            if (!config.isResolvedStatus(originalStatus)) {
                comment.append(StewardConfig.autoResolvingNotificationComment + "\n");
            }
            comment.append(StewardConfig.closingNotificationComment);
        }
        if (!comment.toString().isEmpty()) {
            issue.addComment(new TrakrContent(comment.toString()));
            return true;
        }
        return transitioned;
    }

    private void reopenIssue(TrakrIssue issue) throws TrakrException {
        System.out.println("Issue: " + issue.getKey() + " was resolved, but not actually fixed.");
        boolean transitioned = false;
        if (config.getAutoReopen().isMoveStatus()) {
            List<String> transitions = config.getTransitionsToOpen(issue.getStatus());
            System.out.println("Reopening the issue " + issue.getKey() + ":");
            transitioned = transitionIssue(transitions, issue);
            if (!transitioned) {
                System.out.println("No path defined to Open the issue from \"" + issue.getStatus() + "\" state.");
            }
        }
        StringBuilder comment = new StringBuilder();
        if (config.getAutoReopen().isCommentable(issue, new TrakrContent(StewardConfig.issueNotFixedComment))) {
            comment.append(StewardConfig.issueNotFixedComment);
            if (!transitioned) {
                comment.append("\n").append(StewardConfig.reopenRequestComment);
            }
        }
        if (transitioned) {
            if (!comment.toString().isEmpty()) {
                comment.append("\n");
            }
            comment.append(StewardConfig.reopeningNotificationComment);
        }
        if (!comment.toString().isEmpty()) {
            issue.addComment(new TrakrContent(comment.toString()));
        }
    }

    private boolean transitionIssue(List<String> transitions, TrakrIssue issue) {
        try {
            if (transitions.size() > 1) {
                StringBuilder consoleLog = new StringBuilder();
                consoleLog.append("Transitioning the issue ")
                        .append(issue.getKey()).append(": ").append(transitions.get(0));
                for (int i = 1; i < transitions.size(); i++) {
                    consoleLog.append(" -> ").append(transitions.get(i));
                    TrakrIssueBuilder moveStatus = new TrakrIssueBuilder();
                    moveStatus.setStatus(transitions.get(i));
                    tracker.updateIssue(issue, moveStatus);
                }
                System.out.println(consoleLog.toString());
                return true;
            }
        } catch (Exception e) {
            stewardProcess.addException(e);
        }
        return false;
    }

    private void processFinding(StewardFinding finding) throws StewardException, TrakrException {
        TrakrQuery searchQuery = new TrakrQuery(TrakrQuery.Condition.type, TrakrQuery.Operator.matching, config.getIssueType());
        searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getProject());
        searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getConnector());
        for (String context : finding.getContexts()) {
            searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, context);
        }
        List<TrakrIssue> issues = tracker.searchTrakrIssues(searchQuery);
        if (issues.size() == 0) {
            createTrakrIssueForBug(finding);
        } else if (issues.size() == 1) {
            updateTrakrIssueForBug(issues.get(0), finding);
        } else {
            throw new StewardException("More than one issue listed:\n"
                    + "Labels: " + Arrays.toString(finding.getContexts().toArray()) + "\n"
                    + "Issues: " + Arrays.toString(issues.toArray()));
        }
    }

    private void processFindings() {
        System.out.println("\nProcessing scanned results...");
        for (StewardFinding finding : data.getFindings()) {
            try {
                processFinding(finding);
            } catch (StewardException | TrakrException e) {
                e.printStackTrace();
                stewardProcess.addException(e);
            }
        }
    }

    private void verifyExistingNonClosedIssues() throws TrakrException {
        if (config.isClosingAllowed()) {
            System.out.println("\nVerifying if any existing issues are fixed...");
            TrakrQuery searchQuery = new TrakrQuery(TrakrQuery.Condition.type, TrakrQuery.Operator.matching, config.getIssueType());
            searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getProject());
            searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getConnector());
            for (String context : data.getContexts()) {
                searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, context);
            }
            searchQuery.add(TrakrQuery.Condition.status, TrakrQuery.Operator.not_matching, config.getClosedStatuses());
            List<TrakrIssue> issues = tracker.searchTrakrIssues(searchQuery);
            int count = 0;
            for (TrakrIssue issue : issues) {
                try {
                    if (!isVulnerabilityExists(issue, data.getFindings())) {
                        count++;
                        if (!closeIssue(issue)) {
                            System.out.println(issue.getKey() + ": No action taken now.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    stewardProcess.addException(e);
                }
            }
            if (count == 0) {
                System.out.println("No relevant issues found to resolve/close.");
            }
        }
    }

    static final class StewardProcess {
        private final List<Exception> exceptions;
        private int created;
        private int updated;
        private int commented;

        StewardProcess() {
            this.created = 0;
            this.updated = 0;
            this.commented = 0;
            this.exceptions = new ArrayList<>();
        }

        public int getCreated() {
            return created;
        }

        void setCreated(int created) {
            this.created = created;
        }

        public int getUpdated() {
            return updated;
        }

        void setUpdated(int updated) {
            this.updated = updated;
        }

        public int getCommented() {
            return commented;
        }

        void setCommented(int commented) {
            this.commented = commented;
        }

        void addException(Exception e) {
            this.exceptions.add(e);
        }

        public List<Exception> getExceptions() {
            return exceptions;
        }
    }
}
