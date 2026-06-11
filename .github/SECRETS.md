Repository secrets required for CI / production

Add these in GitHub -> Settings -> Secrets -> Actions for the repository.

- `AWS_ACCESS_KEY_ID` — AWS access key id (optional, only if you publish to S3/ECR from CI)
- `AWS_SECRET_ACCESS_KEY` — AWS secret (optional)
- `GITHUB_TOKEN` — automatically provided in Actions; do NOT add manually unless needed.
- If you prefer to use a PAT for GHCR (not necessary in most cases), add `CR_PAT` (personal access token) with `write:packages` scope.

To add secrets via GitHub CLI (example):

```bash
gh secret set AWS_ACCESS_KEY_ID --body "${AWS_ACCESS_KEY_ID}" \
  --repo satish2694/Employee
gh secret set AWS_SECRET_ACCESS_KEY --body "${AWS_SECRET_ACCESS_KEY}" \
  --repo satish2694/Employee
```

