#!/usr/bin/env bash
# Called as UserPromptSubmit hook – pulls latest changes from remote.
# Uses --ff-only so it never creates a merge commit and fails safely
# when local changes would conflict (instead of silently overwriting).

REPO_DIR="$(git -C "$(dirname "$0")/../.." rev-parse --show-toplevel 2>/dev/null)"
if [ -z "$REPO_DIR" ]; then
  exit 0
fi

OUTPUT=$(git -C "$REPO_DIR" pull --ff-only 2>&1)
EXIT_CODE=$?

if [ $EXIT_CODE -ne 0 ]; then
  echo "[git-sync] Pull failed (non-fast-forward or network error). Run 'git pull' manually."
  echo "[git-sync] $OUTPUT"
  exit 0  # Don't block the session – warn only
fi

# Only print when there were actual changes
if [ "$OUTPUT" != "Already up to date." ]; then
  echo "[git-sync] $OUTPUT"
fi
