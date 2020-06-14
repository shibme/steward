# Steward
[![Download](https://api.bintray.com/packages/shibme/maven/steward/images/download.svg)](https://bintray.com/shibme/maven/steward/_latestVersion)
[![Build Status](https://gitlab.com/shibme/steward/badges/master/pipeline.svg)](https://gitlab.com/shibme/steward/pipelines)

Sync issues from any source to issue tracking systems

## Configuration for consumers

#### You may optionally define a Steward Config 🙄
 - Prepare a steward config file hosted in a static URL ([sample](https://gitlab.com/shibme/steward/-/blob/master/docs/config.json)) defining the workflow and other stuff and set it through `STEWARD_CONFIG` environment variable.

#### If not a config file URL, go with a bunch of environment variables 😬 [Or even combo of both]
`STEWARD_PROJECT_KEY`
 - Issue tracker project key

`STEWARD_ISSUE_TYPE`
 - Issue type

`STEWARD_PRIORITY_P0`
 - Priority to be mapped for P0 issues [Example: Urgent]

`STEWARD_PRIORITY_P1`
 - Priority to be mapped for P1 issues [Example: High]

`STEWARD_PRIORITY_P2`
 - Priority to be mapped for P2 issues [Example: Medium]

`STEWARD_PRIORITY_P3`
 - Priority to be mapped for P3 issues [Example: Low]

`STEWARD_PRIORITY_P4`
 - Priority to be mapped for P4 issues [Example: Very Low]

`STEWARD_TRACKER_NAME`
 - Name of the issue tracker

`STEWARD_TRACKER_ENDPOINT`
 - Issue tracker API endpoint

`STEWARD_TRACKER_USERNAME`
 - Issue tracker username

`STEWARD_TRACKER_PASSWORD`
 - Issue tracker password

`STEWARD_TRACKER_API_KEY`
 - Issue tracker API Key/Token

`STEWARD_DRY_RUN`
 - Dry run [TRUE|FALSE]

`STEWARD_EXIT_CODE_NEW_ISSUES`
 - Exit code when there are new issues

`STEWARD_EXIT_CODE_FAILURE`
 - Exit code on error

`STEWARD_UPDATE_TITLE`
 - Update title if changed [TRUE|FALSE]

`STEWARD_UPDATE_DESCRIPTION`
 - Update description if changed [TRUE|FALSE]

`STEWARD_UPDATE_LABELS`
 - Update labels if changed [TRUE|FALSE]

`STEWARD_PRIORITIZE_UP`
 - Prioritize up if lowered [TRUE|FALSE]

`STEWARD_PRIORITIZE_DOWN`
 - Prioritize down if raised [TRUE|FALSE]

`STEWARD_ASSIGNEE`
 - User to whom issues have to be assigned

`STEWARD_REOPEN_STATUS`
 - Status to be moved to while reopening

`STEWARD_RESOLVED_STATUSES`
 - List of resolved statuses [CSV supported]

`STEWARD_CLOSED_STATUSES`
 - List of closed statuses [CSV supported]

`STEWARD_IGNORE_LABELS`
 - Issues having these labels will be ignored [CSV supported]

`STEWARD_IGNORE_STATUSES`
 - Issues having these statuses will be ignored [CSV supported]

`STEWARD_AUTO_REOPEN_AFTER`
 - Days after which auto-reopen should work [Default 0]

`STEWARD_AUTO_REOPEN_TRANSITION`
 - Transition issues to reopened status if required [TRUE|FALSE]

`STEWARD_AUTO_REOPEN_COMMENT`
 - Comment on issues to reopen if required [TRUE|FALSE]

`STEWARD_AUTO_RESOLVE_AFTER`
 - Days after which auto-resolve should work [Default 7]

`STEWARD_AUTO_RESOLVE_TRANSITION`
 - Transition issues to resolved status if required [TRUE|FALSE]

`STEWARD_AUTO_RESOLVE_COMMENT`
 - Comment on issues to resolve if required [TRUE|FALSE]