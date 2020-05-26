package me.shib.steward;

import java.util.Arrays;
import java.util.List;

enum StewardEnvar {
    STEWARD_CONFIG("Config URL or config file path"),
    STEWARD_PROJECT_KEY("Issue tracker project key"),
    STEWARD_ISSUE_TYPE("Issue type"),
    STEWARD_PRIORITY_P0("Priority to be mapped for P0 issues [Example: Urgent]"),
    STEWARD_PRIORITY_P1("Priority to be mapped for P1 issues [Example: High]"),
    STEWARD_PRIORITY_P2("Priority to be mapped for P2 issues [Example: Medium]"),
    STEWARD_PRIORITY_P3("Priority to be mapped for P3 issues [Example: Low]"),
    STEWARD_PRIORITY_P4("Priority to be mapped for P4 issues [Example: Very Low]"),
    STEWARD_TRACKER_NAME("Name of the issue tracker"),
    STEWARD_TRACKER_ENDPOINT("Issue tracker API endpoint"),
    STEWARD_TRACKER_USERNAME("Issue tracker username"),
    STEWARD_TRACKER_PASSWORD("Issue tracker password"),
    STEWARD_TRACKER_API_KEY("Issue tracker API Key/Token"),
    STEWARD_DRY_RUN("Dry run [TRUE|FALSE]"),
    STEWARD_EXIT_CODE_NEW_ISSUES("Exit code when there are new issues"),
    STEWARD_EXIT_CODE_FAILURE("Exit code on error"),
    STEWARD_UPDATE_TITLE("Update title if changed [TRUE|FALSE]"),
    STEWARD_UPDATE_DESCRIPTION("Update description if changed [TRUE|FALSE]"),
    STEWARD_UPDATE_LABELS("Update labels if changed [TRUE|FALSE]"),
    STEWARD_PRIORITIZE_UP("Prioritize up if lowered [TRUE|FALSE]"),
    STEWARD_PRIORITIZE_DOWN("Prioritize down if raised [TRUE|FALSE]"),
    STEWARD_ASSIGNEE("User to whom issues have to be assigned"),
    STEWARD_REOPEN_STATUS("Status to be moved to while reopening"),
    STEWARD_RESOLVED_STATUSES("List of resolved statuses [CSV supported]"),
    STEWARD_CLOSED_STATUSES("List of closed statuses [CSV supported]"),
    STEWARD_IGNORE_LABELS("Issues having these labels will be ignored [CSV supported]"),
    STEWARD_IGNORE_STATUSES("Issues having these statuses will be ignored [CSV supported]"),
    STEWARD_AUTO_REOPEN_AFTER("Days after which auto-reopen should work [Default 0]"),
    STEWARD_AUTO_REOPEN_TRANSITION("Transition issues to reopened status if required [TRUE|FALSE]"),
    STEWARD_AUTO_REOPEN_COMMENT("Comment on issues to reopen if required [TRUE|FALSE]"),
    STEWARD_AUTO_RESOLVE_AFTER("Days after which auto-resolve should work [Default 7]"),
    STEWARD_AUTO_RESOLVE_TRANSITION("Transition issues to resolved status if required [TRUE|FALSE]"),
    STEWARD_AUTO_RESOLVE_COMMENT("Comment on issues to resolve if required [TRUE|FALSE]");

    private final transient String description;

    StewardEnvar(String description) {
        this.description = description;
    }

    static String getVarDefinitions() {
        StringBuilder varDefinitions = new StringBuilder();
        for (StewardEnvar var : StewardEnvar.values()) {
            varDefinitions.append("\n").append(var).append("\n")
                    .append("\t- ").append(var.description);
        }
        return varDefinitions.toString();
    }

    private String getValue() {
        String val = System.getenv(name());
        if (val != null && val.isEmpty()) {
            return null;
        }
        return val;
    }

    String getAsString() {
        return getValue();
    }

    List<String> getAsList() {
        try {
            return Arrays.asList(getValue().split(","));
        } catch (Exception e) {
            return null;
        }
    }

    boolean getAsBoolean() {
        try {
            return getValue().equalsIgnoreCase("TRUE");
        } catch (Exception e) {
            return false;
        }
    }

    Integer getAsInteger() {
        try {
            return Integer.parseInt(getValue());
        } catch (Exception e) {
            return null;
        }
    }

}
