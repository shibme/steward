package me.shib.steward;

import me.shib.lib.trakr.*;

import java.util.*;

public final class Steward {

    private transient final StewardExecutionResult executionResult;

    private final StewardConfig config;
    private final Trakr tracker;
    private final StewardData data;

    private Steward(StewardData data, StewardConfig config, StewardExecutionResult executionResult) throws StewardException {
        this.executionResult = executionResult;
        this.data = data;
        this.config = config;
        this.tracker = getContextTracker();
    }

    public static StewardExecutionResult process(StewardData data, StewardConfig config) throws StewardException {
        StewardExecutionResult executionResult = new StewardExecutionResult();
        try {
            System.out.println("Findings Identified in " + data.getProjectName() + " [" +
                    data.getToolName() + "]: " + data.getFindings().size());
            if (config != null) {
                Steward steward = new Steward(data, config, executionResult);
                steward.autoResolveIssuesNotInFindings();
                if (!config.isFindingsToIssuesSyncDisabled()) {
                    steward.syncFindingsToIssues();
                }
                executionResult.summarizeCount();
                Integer exitCode = null;
                if (executionResult.getExceptions().size() > 0 && config.getExitCodeOnFailure() != null) {
                    System.out.println("Failure detected. Exiting (" + config.getExitCodeOnFailure() + ").");
                    exitCode = config.getExitCodeOnFailure();
                } else if (executionResult.getCreated() > 0 && config.getExitCodeOnNewIssues() != null) {
                    System.out.println("New issues found. Exiting (" + config.getExitCodeOnNewIssues() + ").");
                    exitCode = config.getExitCodeOnNewIssues();
                }
                if (exitCode != null) {
                    System.exit(exitCode);
                } else if (executionResult.getFindings() > 0 && config.getExitCodeOnIssues() != null) {
                    System.out.println("Unresolved issues found. Exiting (" + config.getExitCodeOnIssues() + ").");
                    System.exit(config.getExitCodeOnIssues());
                }
            }
        } catch (Exception e) {
            if (config != null && config.getExitCodeOnFailure() != null) {
                e.printStackTrace();
                System.out.println("Failure detected. Exiting (" + config.getExitCodeOnFailure() + ").");
                System.exit(config.getExitCodeOnFailure());
            } else {
                e.printStackTrace();
                throw new StewardException(e);
            }
        }
        System.out.println(executionResult);
        return executionResult;
    }

    public static StewardExecutionResult process(StewardData data) throws StewardException {
        return process(data, StewardConfig.getConfig());
    }

    private Trakr getContextTracker() throws StewardException {
        try {
            TrakrQuery query = new TrakrQuery();
            query.add(TrakrQuery.Condition.project, TrakrQuery.Operator.matching, config.getProjectKey());
            query.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getProjectName());
            query.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getToolName());
            for (String key : data.getContexts()) {
                query.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, key);
            }
            Trakr trakr = Trakr.getTrakr(config.getTrackerName(), config.getConnection(),
                    config.getPriorityMap());
            trakr = new ContextTrakr(trakr, query);
            if (config.isDryRun()) {
                trakr = new DummyTrakr(trakr);
            }
            return trakr;
        } catch (TrakrException e) {
            throw new StewardException(e);
        }
    }

    private StewardIssueLifeCycle createIssueForFinding(StewardFinding finding) throws TrakrException {
        Set<String> labels = new LinkedHashSet<>();
        labels.add(data.getProjectName());
        labels.add(data.getToolName());
        labels.addAll(data.getContexts());
        labels.addAll(data.getTags());
        labels.addAll(finding.getContexts());
        labels.addAll(finding.getTags());
        TrakrIssueBuilder issueBuilder = new TrakrIssueBuilder();
        issueBuilder.setProject(config.getProjectKey());
        issueBuilder.setTitle(finding.getTitle());
        issueBuilder.setIssueType(config.getIssueType());
        issueBuilder.setAssignee(finding.getAssignee(config));
        issueBuilder.setPriority(finding.getPriority());
        issueBuilder.setDescription(new TrakrContent(finding.getDescription()));
        issueBuilder.setLabels(new ArrayList<>(labels));
        TrakrIssue issue = tracker.createIssue(issueBuilder);
        System.out.println("\nCreated new issue: " + issue);
        StewardIssueLifeCycle issueLifeCycle = new StewardIssueLifeCycle(issue, true);
        issueLifeCycle.setCreated();
        return issueLifeCycle;
    }

    private StewardIssueLifeCycle syncFindingIntoIssue(TrakrIssue issue, StewardFinding finding)
            throws TrakrException {
        StewardIssueLifeCycle issueLifeCycle = new StewardIssueLifeCycle(issue, true);
        if (config.isIssueCompletelyIgnorable(issue)) {
            System.out.println("Ignoring the issue: " + issue.getKey());
            issueLifeCycle.setIgnored();
            return issueLifeCycle;
        }
        TrakrIssueBuilder issueBuilder = new TrakrIssueBuilder();
        issueBuilder.setProject(config.getProjectKey());
        if (issue.getAssignee() == null && finding.getAssignee(config) != null) {
            issueBuilder.setAssignee(finding.getAssignee(config));
            issueLifeCycle.setAssigned();
        }
        if (config.isUpdateTitle() && !issue.getTitle().contentEquals(finding.getTitle())) {
            issueBuilder.setTitle(finding.getTitle());
            issueLifeCycle.setTitleUpdated();
        }
        if (config.isUpdateDescription() &&
                !tracker.areContentsMatching(new TrakrContent(finding.getDescription()),
                        issue.getDescription())) {
            issueBuilder.setDescription(new TrakrContent(finding.getDescription()));
            issueLifeCycle.setDescriptionUpdated();
        }
        if (config.isUpdateLabels()) {
            Set<String> newSet = new HashSet<>(issue.getLabels());
            newSet.addAll(finding.getContexts());
            newSet.addAll(finding.getTags());
            if (newSet.size() != issue.getLabels().size()) {
                issueBuilder.setLabels(new ArrayList<>(newSet));
                issueLifeCycle.setLabelsUpdated();
            }
        }
        StringBuilder comment = new StringBuilder();
        if (!config.isPriorityChangeIgnored(issue) && (issue.getPriority() == null || (issue.getPriority().getRank() < finding.getPriority().getRank() && config.isPrioritizeDown())
                || (issue.getPriority().getRank() > finding.getPriority().getRank() && config.isPrioritizeUp()))) {
            issueBuilder.setPriority(finding.getPriority());
            System.out.println("Prioritizing " + issue.getKey() + " to " + tracker.getPriorityName(finding.getPriority()) + " based on actual priority.");
            comment.append("Prioritizing to **").append(tracker.getPriorityName(finding.getPriority())).append("** based on actual priority.");
            issueLifeCycle.setPriorityUpdated();
        }
        if (issueLifeCycle.isUpdated()) {
            issue = tracker.updateIssue(issue, issueBuilder);
            if (!comment.toString().isEmpty()) {
                issue.addComment(new TrakrContent(comment.toString()));
                issueLifeCycle.setCommented();
            }
        }
        if (config.isReOpeningAllowedForStatus(issue.getStatus())) {
            reopenIssue(issueLifeCycle);
        } else if (issueLifeCycle.isUpdated()) {
            System.out.println("\nUpdated the issue: " + issue);
        } else {
            System.out.println("\nIssue up-to date: " + issue);
        }
        return issueLifeCycle;
    }

    private Set<String> toLowerCaseSet(Collection<String> list) {
        Set<String> lowerCaseSet = new HashSet<>();
        for (String item : list) {
            if (item != null) {
                lowerCaseSet.add(item.toLowerCase());
            }
        }
        return lowerCaseSet;
    }

    private boolean isVulnerabilityExists(TrakrIssue issue, List<StewardFinding> findings) {
        Set<String> lowerCaseIssueLabelsList = toLowerCaseSet(issue.getLabels());
        for (StewardFinding finding : findings) {
            if (lowerCaseIssueLabelsList.containsAll(toLowerCaseSet(finding.getContexts()))) {
                return true;
            }
        }
        return false;
    }

    private void resolveIssue(StewardIssueLifeCycle issueLifeCycle) throws TrakrException {
        TrakrIssue issue = issueLifeCycle.getIssue();
        if (!config.isAutoResolveAllowedForStatus(issue.getStatus())) {
            System.out.println("Ignoring auto-resolution for the issue: " + issue.getKey());
            issueLifeCycle.setIgnored();
            return;
        }
        System.out.println("Issue: " + issue.getKey() + " was found to be fixed, but hasn't been moved to resolved.");
        boolean transitioned = false;
        String originalStatus = issue.getStatus();
        if (config.getAutoResolve().isTransition(issue)) {
            List<String> transitions = config.getTransitionsToClose(issue.getStatus());
            System.out.println("Closing the issue " + issue.getKey() + ".");
            transitioned = transitionIssue(transitions, issueLifeCycle);
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
            issueLifeCycle.setResolved();
        }
        if (!comment.toString().isEmpty()) {
            issue.addComment(new TrakrContent(comment.toString()));
            issueLifeCycle.setCommented();
        }
    }

    private void reopenIssue(StewardIssueLifeCycle issueLifeCycle) throws TrakrException {
        TrakrIssue issue = issueLifeCycle.getIssue();
        System.out.println("Issue: " + issue.getKey() + " was resolved, but not actually fixed.");
        boolean transitioned = false;
        if (config.getAutoReopen().isTransition(issue)) {
            List<String> transitions = config.getTransitionsToOpen(issue.getStatus());
            System.out.println("Reopening the issue " + issue.getKey() + ":");
            transitioned = transitionIssue(transitions, issueLifeCycle);
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
            issueLifeCycle.setReOpened();
        }
        if (!comment.toString().isEmpty()) {
            issue.addComment(new TrakrContent(comment.toString()));
            issueLifeCycle.setCommented();
        }
    }

    private boolean transitionIssue(List<String> transitions, StewardIssueLifeCycle issueLifeCycle) {
        TrakrIssue issue = issueLifeCycle.getIssue();
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
                System.out.println(consoleLog);
                return true;
            }
        } catch (Exception e) {
            issueLifeCycle.addException(e);
        }
        return false;
    }

    private StewardIssueLifeCycle syncFinding(StewardFinding finding) throws StewardException, TrakrException {
        StewardIssueLifeCycle issueLifeCycle;
        TrakrQuery searchQuery = new TrakrQuery(TrakrQuery.Condition.type, TrakrQuery.Operator.matching, config.getIssueType());
        searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getProjectName());
        searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getToolName());
        for (String context : finding.getContexts()) {
            searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, context);
        }
        List<TrakrIssue> issues = tracker.searchTrakrIssues(searchQuery);
        if (issues.size() == 0) {
            issueLifeCycle = createIssueForFinding(finding);
        } else if (issues.size() == 1) {
            issueLifeCycle = syncFindingIntoIssue(issues.get(0), finding);
        } else {
            throw new StewardException("More than one issue listed:\n"
                    + "Labels: " + Arrays.toString(finding.getContexts().toArray()) + "\n"
                    + "Issues: " + Arrays.toString(issues.toArray()));
        }
        return issueLifeCycle;
    }

    private void syncFindingsToIssues() {
        System.out.println("\nProcessing scanned results...");
        for (StewardFinding finding : data.getFindings()) {
            try {
                executionResult.addIssueLifeCycle(syncFinding(finding));
            } catch (StewardException | TrakrException e) {
                e.printStackTrace();
                executionResult.addException(e);
            }
        }
    }

    private void autoResolveIssuesNotInFindings() throws StewardException {
        try {
            if (config.isAutoResolveAllowed()) {
                System.out.println("\nVerifying if any existing issues are fixed...");
                TrakrQuery searchQuery = new TrakrQuery(TrakrQuery.Condition.type, TrakrQuery.Operator.matching, config.getIssueType());
                searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getProjectName());
                searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, data.getToolName());
                for (String context : data.getContexts()) {
                    searchQuery.add(TrakrQuery.Condition.label, TrakrQuery.Operator.matching, context);
                }
                searchQuery.add(TrakrQuery.Condition.status, TrakrQuery.Operator.not_matching, config.getClosedStatuses());
                List<TrakrIssue> issues = tracker.searchTrakrIssues(searchQuery);
                int count = 0;
                for (TrakrIssue issue : issues) {
                    StewardIssueLifeCycle issueLifeCycle = new StewardIssueLifeCycle(issue, false);
                    if (!config.isIssueCompletelyIgnorable(issue)) {
                        try {
                            if (!isVulnerabilityExists(issue, data.getFindings())) {
                                count++;
                                resolveIssue(issueLifeCycle);
                                if (!issueLifeCycle.isResolved()) {
                                    System.out.println(issue.getKey() + ": Auto-resolution was not done.");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            issueLifeCycle.addException(e);
                        }
                    } else {
                        issueLifeCycle.setIgnored();
                    }
                }
                if (count == 0) {
                    System.out.println("No relevant issues found to resolve/close.");
                }
            }
        } catch (TrakrException e) {
            throw new StewardException(e);
        }
    }

}
