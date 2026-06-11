#!/usr/bin/env bash
set -euo pipefail

# Usage: run from repository root
# ./scripts/create_and_push_github.sh

REPO_NAME="Employee"
GH_USER="satish2694"
REMOTE_URL_SSH="git@github.com:${GH_USER}/${REPO_NAME}.git"
REMOTE_URL_HTTPS="https://github.com/${GH_USER}/${REPO_NAME}.git"

echo "Running GitHub repo creation and push helper"
ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT_DIR"

# Ensure git repo exists
if [ ! -d .git ]; then
  echo "Initializing git repository"
  git init
fi

# Ensure branch main
git branch -M main || true

if command -v gh >/dev/null 2>&1; then
  echo "gh CLI found. Using gh to create repo and push."
  if ! gh auth status >/dev/null 2>&1; then
    echo "You are not authenticated with gh. Please run 'gh auth login' now."
    gh auth login
  fi

  # Create repo (will fail if exists)
  if gh repo view ${GH_USER}/${REPO_NAME} >/dev/null 2>&1; then
    echo "Repository ${GH_USER}/${REPO_NAME} already exists on GitHub. Skipping creation."
  else
    echo "Creating repository ${GH_USER}/${REPO_NAME} on GitHub (private)."
    gh repo create ${GH_USER}/${REPO_NAME} --private --source=. --remote=origin --push
    echo "Repository created and code pushed via gh."
    exit 0
  fi

  # If repo exists, set origin (use HTTPS) and push
  git remote remove origin 2>/dev/null || true
  git remote add origin "$REMOTE_URL_HTTPS"
  echo "Pushing local main to origin via HTTPS..."
  # git will use gh's credential helper to authenticate
  if git push -u origin main 2>&1 | grep -q "Permission denied\|fatal"; then
    echo "HTTPS push failed. Trying with explicit gh credential..."
    git -c credential.helper= -c credential.helper='!gh auth token' push -u origin main
  fi
  echo "Pushed."

  # Offer to set secrets
  read -p "Would you like to add repository secrets using gh? (y/N): " add_secrets
  if [[ "$add_secrets" =~ ^[Yy]$ ]]; then
    read -p "AWS_ACCESS_KEY_ID (leave empty to skip): " AWS_ACCESS_KEY_ID
    if [ -n "$AWS_ACCESS_KEY_ID" ]; then
      gh secret set AWS_ACCESS_KEY_ID --body "$AWS_ACCESS_KEY_ID" --repo ${GH_USER}/${REPO_NAME}
      read -p "AWS_SECRET_ACCESS_KEY: " AWS_SECRET_ACCESS_KEY
      gh secret set AWS_SECRET_ACCESS_KEY --body "$AWS_SECRET_ACCESS_KEY" --repo ${GH_USER}/${REPO_NAME}
      echo "AWS secrets set."
    fi
  fi

else
  echo "gh CLI not found. Falling back to GitHub API using GITHUB_PAT environment variable."
  if [ -z "${GITHUB_PAT:-}" ]; then
    echo "ERROR: GITHUB_PAT not set. Export a personal access token with 'repo' scope in GITHUB_PAT and re-run this script." >&2
    exit 1
  fi

  # Create repo via API
  echo "Creating repository via GitHub API"
  create_resp=$(curl -s -o /dev/stderr -w "%{http_code}" -H "Authorization: token ${GITHUB_PAT}" \
    -d "{\"name\": \"${REPO_NAME}\", \"private\": true}" https://api.github.com/user/repos)
  if [ "$create_resp" != "201" ] && [ "$create_resp" != "422" ]; then
    echo "Warning: unexpected response creating repo: $create_resp"
  fi

  # Set remote and push using HTTPS with PAT
  git remote remove origin 2>/dev/null || true
  git remote add origin "${REMOTE_URL_HTTPS}"
  # Push using PAT in URL to authenticate (note: this leaves remote URL without PAT)
  echo "Pushing using HTTPS with PAT"
  git push https://${GH_USER}:${GITHUB_PAT}@github.com/${GH_USER}/${REPO_NAME}.git -u main

  echo "Repository created and pushed."
  echo "To set secrets without gh, use GitHub UI."
fi

echo "Done."

