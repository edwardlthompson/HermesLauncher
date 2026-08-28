package main

import (
	"strings"
	"testing"
)

func TestGreet(t *testing.T) {
	if Greet() != "hello FOSS" {
		t.Fatalf("unexpected greeting: %q", Greet())
	}
}

func TestAboutSummary(t *testing.T) {
	got := AboutSummary()
	if !strings.Contains(got, AppVersion) || !strings.Contains(got, "donate") {
		t.Fatalf("about missing version/donate: %q", got)
	}
}

func TestSanitizeCrashText(t *testing.T) {
	in := "user@example.com C:\\Users\\ada\\secret token=abc /home/ada/.env"
	got := SanitizeCrashText(in)
	if strings.Contains(got, "user@example.com") || strings.Contains(got, `Users\ada`) {
		t.Fatalf("leaked PII: %q", got)
	}
	if !strings.Contains(got, "<redacted-email>") {
		t.Fatalf("expected email redaction: %q", got)
	}
}
