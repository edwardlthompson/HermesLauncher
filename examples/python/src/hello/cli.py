"""CLI entry point for Golden Path Python stub."""

import argparse
import sys

from hello.about import about_summary
from hello.greet import greet, validate_name


def main() -> None:
    """Run the hello CLI."""
    parser = argparse.ArgumentParser(description="Golden Path Python CLI stub")
    parser.add_argument("name", nargs="?", default="", help="Name to greet")
    parser.add_argument("--about", action="store_true", help="Print About (version + donate)")
    args = parser.parse_args()

    if args.about:
        print(about_summary())
        return

    try:
        validated = validate_name(args.name)
        print(greet(validated))
    except ValueError as exc:
        print(f"Error: {exc}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
