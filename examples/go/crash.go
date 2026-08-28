package main

import (
	"regexp"
	"strings"
)

const maxStackLines = 200

var (
	emailRe    = regexp.MustCompile(`[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}`)
	winHomeRe  = regexp.MustCompile(`(?i)C:\\Users\\[^\\]+\\`)
	unixHomeRe = regexp.MustCompile(`/(?:home|Users)/[^/\s]+/`)
	tokenRe    = regexp.MustCompile(`(?i)(?:api[_-]?key|token)\s*[:=]\s*\S+`)
)

// SanitizeCrashText redacts email, home paths, and token-like assignments.
func SanitizeCrashText(text string) string {
	out := emailRe.ReplaceAllString(text, "<redacted-email>")
	out = winHomeRe.ReplaceAllString(out, "<redacted-home>")
	out = unixHomeRe.ReplaceAllString(out, "<redacted-home>/")
	out = tokenRe.ReplaceAllString(out, "<redacted-secret>")
	lines := strings.Split(out, "\n")
	if len(lines) > maxStackLines {
		lines = lines[:maxStackLines]
	}
	return strings.Join(lines, "\n")
}
