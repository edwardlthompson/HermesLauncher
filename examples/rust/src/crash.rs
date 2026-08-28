//! Crash text sanitizer: redact email, home paths, and token assignments.
//! Std-only (no extra crates) so the Golden Path stays zero-dep.

const MAX_STACK_LINES: usize = 200;

pub fn sanitize(text: &str) -> String {
    let mut out = String::with_capacity(text.len());
    for raw in text.split_inclusive(char::is_whitespace) {
        out.push_str(&redact_token(raw));
    }
    let joined: Vec<&str> = out.lines().collect();
    if joined.len() <= MAX_STACK_LINES {
        return out;
    }
    joined[..MAX_STACK_LINES].join("\n")
}

fn redact_token(word: &str) -> String {
    let core = word.trim_end_matches(|c: char| c.is_whitespace() || c == ',' || c == ';');
    let trail = &word[core.len()..];
    if core.contains('@') && core.contains('.') {
        return format!("<redacted-email>{trail}");
    }
    let lower = core.to_ascii_lowercase();
    if lower.contains("token=") || lower.contains("api_key=") || lower.contains("apikey=") {
        return format!("<redacted-secret>{trail}");
    }
    if let Some(rest) = strip_prefix_ci(core, r"C:\Users\") {
        if rest.split('\\').next().is_some() {
            return format!("<redacted-home>{trail}");
        }
    }
    if let Some(rest) = core.strip_prefix("/home/") {
        if rest.split('/').next().is_some() {
            return format!("<redacted-home>/{trail}");
        }
    }
    if let Some(rest) = core.strip_prefix("/Users/") {
        if rest.split('/').next().is_some() {
            return format!("<redacted-home>/{trail}");
        }
    }
    word.to_string()
}

fn strip_prefix_ci<'a>(s: &'a str, prefix: &str) -> Option<&'a str> {
    if s.len() >= prefix.len() && s[..prefix.len()].eq_ignore_ascii_case(prefix) {
        Some(&s[prefix.len()..])
    } else {
        None
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn redacts_email_and_homes() {
        let got = sanitize(r"user@example.com C:\Users\ada\x token=abc /home/ada/.env");
        assert!(got.contains("<redacted-email>"));
        assert!(!got.contains("user@example.com"));
        assert!(got.contains("<redacted-home>"));
        assert!(got.contains("<redacted-secret>"));
    }
}
