"""CLI About slice: version + donate URL (no crash payload)."""

APP_VERSION = "0.1.0"
DONATE_URL = "https://github.com/sponsors"


def about_summary() -> str:
    """Return a one-line About string."""
    return f"golden-path {APP_VERSION} donate {DONATE_URL}"
