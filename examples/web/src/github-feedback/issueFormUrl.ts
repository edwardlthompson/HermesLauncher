export const MAX_QUERY_CHARS = 1800;

export type IssueFormFields = Record<string, string | undefined>;

export function isPlaceholderRepo(repo: string): boolean {
  const trimmed = repo.trim();
  return !trimmed || trimmed.toUpperCase() === "OWNER/REPO";
}

export function crashTitle(fingerprint: string, exceptionType: string): string {
  const fp = fingerprint
    .replace(/[^a-f0-9]/gi, "")
    .slice(0, 12)
    .toLowerCase();
  const kind = (exceptionType || "Error").split(/[^A-Za-z0-9_.$]/)[0] || "Error";
  return `[crash] ${fp} ${kind}`;
}

export function buildIssueFormUrl(
  repo: string,
  template: string,
  fields: IssueFormFields,
): { url: string; clipboardMarkdown?: string; bodyTooLarge: boolean } {
  const trimmed = repo.trim();
  if (isPlaceholderRepo(trimmed)) {
    return { url: "", bodyTooLarge: false };
  }
  const base = `https://github.com/${trimmed}/issues/new`;
  const params = new URLSearchParams();
  params.set("template", template);
  if (fields.title) params.set("title", fields.title);
  if (fields.labels) params.set("labels", fields.labels);
  for (const [key, value] of Object.entries(fields)) {
    if (!value || key === "title" || key === "labels") continue;
    params.set(key, value);
  }
  const full = `${base}?${params.toString()}`;
  if (full.length <= MAX_QUERY_CHARS) {
    return { url: full, bodyTooLarge: false };
  }
  const short = new URLSearchParams();
  short.set("template", template);
  if (fields.title) short.set("title", fields.title);
  const markdown =
    fields.stack || fields.description || fields.reproduction || fields.problem || "";
  return {
    url: `${base}?${short.toString()}`,
    clipboardMarkdown: markdown,
    bodyTooLarge: true,
  };
}
