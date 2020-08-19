package me.shib.steward;

import me.shib.lib.trakr.TrakrPriority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class StewardFinding {

    private final String title;
    private final TrakrPriority priority;
    private final Set<String> contexts;
    private final Set<String> tags;
    private String description;
    private String assignee;

    public StewardFinding(String title, TrakrPriority priority) {
        this.title = title;
        this.priority = priority;
        this.contexts = new LinkedHashSet<>();
        this.tags = new LinkedHashSet<>();
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

    String getTitle() {
        return title;
    }

    TrakrPriority getPriority() {
        return priority;
    }

    String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    Set<String> getContexts() {
        return contexts;
    }

    Set<String> getTags() {
        return tags;
    }

    String getAssignee(StewardConfig config) {
        if (assignee != null && !assignee.isEmpty()) {
            return assignee;
        }
        if (config.getAssignee() != null && !config.getAssignee().isEmpty()) {
            return config.getAssignee();
        }
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
