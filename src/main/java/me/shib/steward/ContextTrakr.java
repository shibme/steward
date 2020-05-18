package me.shib.steward;

import me.shib.lib.trakr.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ContextTrakr extends Trakr {

    private final transient Trakr trakr;
    private final transient Map<String, TrakrIssue> contextIssueMap;

    ContextTrakr(Trakr trakr, TrakrQuery contextQuery) throws TrakrException {
        super(trakr.getConnection(), trakr.getPriorityMap());
        this.trakr = trakr;
        this.contextIssueMap = new HashMap<>();
        for (TrakrIssue issue : trakr.searchTrakrIssues(contextQuery)) {
            addToContext(issue);
        }
    }

    void addToContext(TrakrIssue issue) {
        if (issue != null) {
            contextIssueMap.put(issue.getKey(), issue);
        }
    }

    @Override
    public TrakrContent.Type getContentType() {
        return trakr.getContentType();
    }

    @Override
    public TrakrIssue createIssue(TrakrIssueBuilder creator) throws TrakrException {
        TrakrIssue trakrIssue = trakr.createIssue(creator);
        addToContext(trakrIssue);
        return trakrIssue;
    }

    @Override
    public TrakrIssue updateIssue(TrakrIssue issue, TrakrIssueBuilder updater) throws TrakrException {
        issue = trakr.updateIssue(issue, updater);
        addToContext(issue);
        return issue;
    }

    private boolean isCaseInsensitiveStringInList(String str, List<String> list) {
        for (String item : list) {
            if (item.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLabelsInIssue(TrakrIssue issue, List<String> labels) {
        for (String label : labels) {
            if (isCaseInsensitiveStringInList(label, issue.getLabels())) {
                return true;
            }
        }
        return false;
    }

    private boolean isLabelsNotInIssue(TrakrIssue issue, List<String> labels) {
        for (String label : labels) {
            if (isCaseInsensitiveStringInList(label, issue.getLabels())) {
                return false;
            }
        }
        return true;
    }

    private List<TrakrIssue> contextualSearch(TrakrQuery query) throws TrakrException {
        List<TrakrIssue> filteredIssues = new ArrayList<>(contextIssueMap.values());
        for (TrakrQuery.TrakrQueryItem queryItem : query.getQueryItems()) {
            switch (queryItem.getCondition()) {
                case project:
                    List<TrakrIssue> projectFilterList = new ArrayList<>();
                    for (TrakrIssue issue : filteredIssues) {
                        if (queryItem.getValues().contains(issue.getProjectKey())) {
                            projectFilterList.add(issue);
                        }
                    }
                    filteredIssues = projectFilterList;
                    break;
                case label:
                    List<TrakrIssue> labelFilterList = new ArrayList<>();
                    for (TrakrIssue issue : filteredIssues) {
                        if (null == issue.getLabels()) {
                            issue.refresh();
                        }
                        switch (queryItem.getOperator()) {
                            case matching:
                                if (isLabelsInIssue(issue, queryItem.getValues())) {
                                    labelFilterList.add(issue);
                                }
                                break;
                            case not_matching:
                                if (isLabelsNotInIssue(issue, queryItem.getValues())) {
                                    labelFilterList.add(issue);
                                }
                                break;
                        }
                    }
                    filteredIssues = labelFilterList;
                    break;
                case status:
                    List<TrakrIssue> statusFilterList = new ArrayList<>();
                    for (TrakrIssue issue : filteredIssues) {
                        switch (queryItem.getOperator()) {
                            case matching:
                                if (queryItem.getValues().contains(issue.getStatus())) {
                                    statusFilterList.add(issue);
                                }
                                break;
                            case not_matching:
                                if (!queryItem.getValues().contains(issue.getStatus())) {
                                    statusFilterList.add(issue);
                                }
                                break;
                        }
                    }
                    filteredIssues = statusFilterList;
                    break;
                case type:
                    List<TrakrIssue> typeFilterList = new ArrayList<>();
                    for (TrakrIssue issue : filteredIssues) {
                        switch (queryItem.getOperator()) {
                            case matching:
                                if (queryItem.getValues().contains(issue.getType())) {
                                    typeFilterList.add(issue);
                                }
                                break;
                            case not_matching:
                                if (!queryItem.getValues().contains(issue.getType())) {
                                    typeFilterList.add(issue);
                                }
                                break;
                        }
                    }
                    filteredIssues = typeFilterList;
                    break;
            }
        }
        return filteredIssues;
    }

    @Override
    public List<TrakrIssue> searchTrakrIssues(TrakrQuery query) throws TrakrException {
        return contextualSearch(query);
    }
}
