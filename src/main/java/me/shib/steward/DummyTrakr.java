package me.shib.steward;

import me.shib.lib.trakr.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DummyTrakr extends Trakr {

    private final Trakr trakr;

    DummyTrakr(Trakr trakr, Connection connection, Map<String, TrakrPriority> priorityMap) {
        super(connection, priorityMap);
        this.trakr = trakr;
        System.out.println("Connecting to Trakr in read-only mode");
    }

    @Override
    public TrakrContent.Type getContentType() {
        return trakr.getContentType();
    }

    @Override
    public TrakrIssue createIssue(TrakrIssueBuilder creator) {
        return new DummyIssue(this, creator);
    }

    @Override
    public TrakrIssue updateIssue(TrakrIssue issue, TrakrIssueBuilder updater) {
        return new DummyIssue(this, issue);
    }

    @Override
    public List<TrakrIssue> searchTrakrIssues(TrakrQuery query) throws TrakrException {
        List<TrakrIssue> trakrIssues = trakr.searchTrakrIssues(query);
        List<TrakrIssue> dummyIssues = new ArrayList<>();
        for (TrakrIssue trakrIssue : trakrIssues) {
            dummyIssues.add(new DummyIssue(trakr, trakrIssue));
        }
        return dummyIssues;
    }
}
