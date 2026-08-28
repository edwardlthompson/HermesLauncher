package main

const (
	AppVersion = "0.1.0"
	DonateURL  = "https://github.com/sponsors"
)

// AboutSummary is the CLI About slice (version + donate, no crash payload).
func AboutSummary() string {
	return "golden-path " + AppVersion + " donate " + DonateURL
}
