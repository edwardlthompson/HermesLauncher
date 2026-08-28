"""Tests for hello.about."""

from hello.about import APP_VERSION, about_summary


def test_about_includes_version_and_donate() -> None:
    text = about_summary()
    assert APP_VERSION in text
    assert "donate" in text
