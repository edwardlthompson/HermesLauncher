# Winget publish runbook

Use this when a **child** repo ships a native Windows installer. This template only generates a stub. `[HUMAN]` opens the `microsoft/winget-pkgs` PR.

## Generate the stub

```bash
bash scripts/generate-winget-manifest.sh Example.Publisher.App 1.2.3 packaging/winget
bash scripts/validate-winget-stub.sh packaging/winget/manifest.stub.yaml
```

`generate-winget-manifest.sh` writes `PackageIdentifier`, `PackageVersion`, `License`, and `InstallerSha256`. `validate-winget-stub.sh` fails if those keys are missing. A missing file is a skip (CI generates the stub in `release.yml` before the check).

## Fill before submit

1. Replace `Example.Publisher.App` and `Example Publisher` with the real identity.
2. Set `InstallerUrl` to the GitHub Release asset (HTTPS).
3. Set `InstallerSha256` to the SHA-256 of that asset. Never invent a hash.
4. Keep `License: MIT` unless the child repo changed `LICENSE`.
5. Re-run `bash scripts/validate-winget-stub.sh packaging/winget/manifest.stub.yaml`.

Do not commit live installer URLs that embed tokens. Do not commit `.env`.

## Submit

1. Fork [microsoft/winget-pkgs](https://github.com/microsoft/winget-pkgs).
2. Add the singleton (or versioned) manifest under `manifests/<first-letter>/<Publisher>/<Package>/<Version>/`.
3. Open a PR. Winget validation bots must pass.
4. `[HUMAN]` owns the publish click. Agents may draft the YAML only.

## Template vs child

On **this** template, the stub is an example. Do not file a Winget PR for `agent-project-bootstrap`. Child desktop apps follow this runbook after `/prerelease` and a GitHub Release with the installer attached.
