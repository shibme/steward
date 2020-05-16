package me.shib.steward;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class StewardData {

    private final String project;
    private final String connector;
    private final Set<String> contexts;
    private final Set<String> tags;
    private final List<StewardFinding> findings;

    public StewardData(String project, String connector) {
        this.project = project;
        this.connector = connector;
        this.contexts = new LinkedHashSet<>();
        this.tags = new LinkedHashSet<>();
        this.findings = new ArrayList<>();
    }

    public void addContext(String context) {
        this.contexts.add(context);
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void addFinding(StewardFinding finding) {
        for (String context : contexts) {
            finding.addContext(context);
        }
        for (String tag : tags) {
            finding.addTag(tag);
        }
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
