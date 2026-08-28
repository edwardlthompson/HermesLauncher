/** Parse Web Share Target GET params into Feedback description text. */

export function shareTargetDescription(params: URLSearchParams): string {
  const parts = [params.get("title"), params.get("text"), params.get("url")]
    .map((value) => value?.trim() ?? "")
    .filter(Boolean);
  return parts.join("\n");
}
