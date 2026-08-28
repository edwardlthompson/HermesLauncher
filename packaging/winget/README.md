# Winget Manifest Stub

Publish runbook: [`docs/WINGET.md`](../../docs/WINGET.md).

```bash
bash scripts/generate-winget-manifest.sh Example.Publisher.App 1.2.3 packaging/winget
bash scripts/validate-winget-stub.sh packaging/winget/manifest.stub.yaml
```

Submit the filled YAML to https://github.com/microsoft/winget-pkgs. `[HUMAN]` opens that PR.
