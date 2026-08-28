//! CLI About slice: version + donate URL (no crash payload).

pub const APP_VERSION: &str = "0.1.0";
pub const DONATE_URL: &str = "https://github.com/sponsors";

pub fn summary() -> String {
    format!("golden-path {APP_VERSION} donate {DONATE_URL}")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn includes_version_and_donate() {
        let text = summary();
        assert!(text.contains(APP_VERSION));
        assert!(text.contains("donate"));
    }
}
