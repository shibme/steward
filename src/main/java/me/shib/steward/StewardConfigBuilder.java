package me.shib.steward;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shib.lib.trakr.Trakr;
import me.shib.lib.trakr.TrakrPriority;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

class StewardConfigBuilder {

    private static transient final Gson gson = new GsonBuilder().create();
    private static transient StewardConfig config;

    private static String getConfigFromURL(String configURL) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(configURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line).append("\n");
        }
        rd.close();
        return result.toString();
    }

    private static String readFromFile(File file) throws IOException {
        if (!file.exists() || file.isDirectory()) {
            return "";
        }
        StringBuilder contentBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            contentBuilder.append(line).append("\n");
        }
        br.close();
        return contentBuilder.toString();
    }

    private static StewardConfig buildConfig(String configURI) {
        try {
            String configJson = null;
            if (configURI != null && !configURI.isEmpty()) {
                if (configURI.trim().toLowerCase().startsWith("http://") ||
                        configURI.trim().toLowerCase().startsWith("https://")) {
                    configJson = getConfigFromURL(configURI);
                } else {
                    configJson = readFromFile(new File(configURI));
                }
            }
            if (configJson == null || configJson.isEmpty()) {
                throw new StewardException("Please provide a valid config file or URL");
            }
            StewardConfig config = gson.fromJson(configJson, StewardConfig.class);
            backFillFromEnv(config);
            config.validate();
            return config;
        } catch (Exception e) {
            return null;
        }
    }

    static synchronized StewardConfig buildConfig() {
        if (config == null) {
            config = buildConfig(StewardEnvar.STEWARD_CONFIG.getAsString());
        }
        return config;
    }

    private static void backFillFromEnv(StewardConfig config) {
        String projectKey = StewardEnvar.STEWARD_PROJECT_KEY.getAsString();
        if (projectKey != null) {
            config.setProjectKey(projectKey);
        }
        String issueType = StewardEnvar.STEWARD_ISSUE_TYPE.getAsString();
        if (issueType != null) {
            config.setIssueType(issueType);
        }
        Map<TrakrPriority, String> priorityMap = config.getPriorityMap();
        String p0 = StewardEnvar.STEWARD_PRIORITY_P0.getAsString();
        if (p0 != null) {
            priorityMap.put(TrakrPriority.P0, p0);
        }
        String p1 = StewardEnvar.STEWARD_PRIORITY_P1.getAsString();
        if (p1 != null) {
            priorityMap.put(TrakrPriority.P1, p1);
        }
        String p2 = StewardEnvar.STEWARD_PRIORITY_P2.getAsString();
        if (p2 != null) {
            priorityMap.put(TrakrPriority.P2, p2);
        }
        String p3 = StewardEnvar.STEWARD_PRIORITY_P3.getAsString();
        if (p3 != null) {
            priorityMap.put(TrakrPriority.P3, p3);
        }
        String p4 = StewardEnvar.STEWARD_PRIORITY_P4.getAsString();
        if (p4 != null) {
            priorityMap.put(TrakrPriority.P4, p4);
        }
        config.setPriorityMap(priorityMap);
        String trackerName = StewardEnvar.STEWARD_TRACKER_NAME.getAsString();
        try {
            Trakr.Type type = Trakr.Type.valueOf(trackerName);
            config.setTrackerName(type);
        } catch (Exception ignored) {
        }
        String trackerEndpoint = StewardEnvar.STEWARD_TRACKER_ENDPOINT.getAsString();
        String trackerApiKey = StewardEnvar.STEWARD_TRACKER_API_KEY.getAsString();
        String trackerUsername = StewardEnvar.STEWARD_TRACKER_USERNAME.getAsString();
        String trackerPassword = StewardEnvar.STEWARD_TRACKER_PASSWORD.getAsString();
        Trakr.Connection connection = config.getConnection();
        if (trackerEndpoint != null) {
            if (trackerApiKey != null) {
                connection = new Trakr.Connection(trackerEndpoint, trackerApiKey);
            } else if (trackerUsername != null && trackerPassword != null) {
                connection = new Trakr.Connection(trackerEndpoint, trackerUsername, trackerPassword);
            }
        }
        config.setConnection(connection);
        if (StewardEnvar.STEWARD_DRY_RUN.getAsBoolean()) {
            config.setDryRun(true);
        }
        if (StewardEnvar.STEWARD_UPDATE_SUMMARY.getAsBoolean()) {
            config.setUpdateSummary(true);
        }
        if (StewardEnvar.STEWARD_UPDATE_DESCRIPTION.getAsBoolean()) {
            config.setUpdateDescription(true);
        }
        if (StewardEnvar.STEWARD_UPDATE_LABELS.getAsBoolean()) {
            config.setUpdateLabels(true);
        }
        if (StewardEnvar.STEWARD_PRIORITIZE_UP.getAsBoolean()) {
            config.setPrioritizeUp(true);
        }
        if (StewardEnvar.STEWARD_PRIORITIZE_DOWN.getAsBoolean()) {
            config.setPrioritizeDown(true);
        }
        String assignee = StewardEnvar.STEWARD_ASSIGNEE.getAsString();
        if (assignee != null) {
            config.setAssignee(assignee);
        }
        String reOpenStatus = StewardEnvar.STEWARD_REOPEN_STATUS.getAsString();
        if (reOpenStatus != null) {
            config.setReOpenStatus(reOpenStatus);
        }
        List<String> resolvedStatuses = StewardEnvar.STEWARD_RESOLVED_STATUSES.getAsList();
        if (resolvedStatuses != null) {
            config.setResolvedStatuses(resolvedStatuses);
        }
        List<String> closedStatuses = StewardEnvar.STEWARD_CLOSED_STATUSES.getAsList();
        if (closedStatuses != null) {
            config.setClosedStatuses(closedStatuses);
        }
        List<String> ignoreLabels = StewardEnvar.STEWARD_IGNORE_LABELS.getAsList();
        if (ignoreLabels != null) {
            config.setIgnoreForLabels(ignoreLabels);
        }
        List<String> ignoreStatuses = StewardEnvar.STEWARD_IGNORE_STATUSES.getAsList();
        if (ignoreStatuses != null) {
            config.setIgnoreForStatuses(ignoreStatuses);
        }
        StewardConfig.Changes autoReopen = config.getAutoReopen();
        if (autoReopen == null) {
            autoReopen = new StewardConfig.Changes(false, false, 0);
        }
        if (StewardEnvar.STEWARD_AUTO_REOPEN_TRANSITION.getAsBoolean()) {
            autoReopen.setTransition(true);
        }
        if (StewardEnvar.STEWARD_AUTO_REOPEN_COMMENT.getAsBoolean()) {
            autoReopen.setComment(true);
        }
        config.setAutoReopen(autoReopen);

        StewardConfig.Changes autoResolve = config.getAutoResolve();
        if (autoResolve == null) {
            autoResolve = new StewardConfig.Changes(false, false, 0);
        }
        if (StewardEnvar.STEWARD_AUTO_RESOLVE_TRANSITION.getAsBoolean()) {
            autoResolve.setTransition(true);
        }
        if (StewardEnvar.STEWARD_AUTO_RESOLVE_COMMENT.getAsBoolean()) {
            autoResolve.setComment(true);
        }
        config.setAutoResolve(autoResolve);
    }

}
