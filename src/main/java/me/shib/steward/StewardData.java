package me.shib.steward;

import java.util.*;

public final class StewardData {

    private static final transient String stewardLabel = "Steward";

    private final String project;
    private final String connector;
    private final Set<String> contexts;
    private final Set<String> tags;
    private final List<StewardFinding> findings;

    public StewardData(String project, String connector) {
        this.project = project;
        this.connector = connector;
        this.contexts = new LinkedHashSet<>();
        this.addContext(stewardLabel);
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

    String getProject() {
        return project;
    }

    String getConnector() {
        return connector;
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
