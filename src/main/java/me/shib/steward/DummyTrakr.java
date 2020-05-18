package me.shib.steward;

import me.shib.lib.trakr.*;

import java.util.ArrayList;
import java.util.List;

public final class DummyTrakr extends Trakr {

    private final Trakr trakr;

    DummyTrakr(Trakr trakr) {
        super(trakr.getConnection(), trakr.getPriorityMap());
        this.trakr = trakr;
        System.out.println("Dry-run enabled...");
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
