package me.shib.steward;

import java.util.*;

public final class StewardData {

    private final String projectName;
    private final String toolName;
    private final Set<String> contexts;
    private final Set<String> tags;
    private final List<StewardFinding> findings;

    public StewardData(String projectName, String toolName) {
        this.projectName = projectName;
        this.toolName = toolName;
        this.contexts = new LinkedHashSet<>();
        this.tags = new LinkedHashSet<>();
        this.findings = new ArrayList<>();
    }

    public void addContext(String context) {
        this.contexts.add(context);
    }

    public void addContexts(Collection<String> contexts) {
        this.contexts.addAll(contexts);
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void addTags(Collection<String> tags) {
        this.tags.addAll(tags);
    }

    public void addFinding(StewardFinding finding) {
        finding.addContexts(contexts);
        finding.addTags(tags);
        this.findings.add(finding);
    }

    String getProjectName() {
        return projectName;
    }

    String getToolName() {
        return toolName;
    }

    Set<String> getContexts() {
        return contexts;
    }

    Set<String> getTags() {
        return tags;
    }

    List<StewardFinding> getFindings() {
        return findings;
    }
}
