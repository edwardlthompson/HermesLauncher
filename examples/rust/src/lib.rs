//! Testable Golden Path library (About + crash sanitize).

pub mod about;
pub mod crash;

pub fn greet() -> &'static str {
    "hello FOSS"
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn greets() {
        assert_eq!(greet(), "hello FOSS");
    }
}
