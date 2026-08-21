import { assetUrl } from "../assetUrl";
import { withQuietDonate } from "./donate";
import type { DonationConfig, DonationLink } from "./types";

export const DEFAULT_VENMO_URL = "https://venmo.com/code?user_id=1857304970395648420";

export function primaryDonateUrl(config: DonationConfig): string {
  return config.links[0]?.url || DEFAULT_VENMO_URL;
}

function isLink(value: unknown): value is DonationLink {
  if (!value || typeof value !== "object") return false;
  const v = value as DonationLink;
  return typeof v.label === "string" && typeof v.url === "string";
}

export function normalizeDonations(raw: unknown): DonationConfig {
  if (!raw || typeof raw !== "object") {
    return { enabled: false, message: "", links: [] };
  }
  const obj = raw as DonationConfig;
  const links = Array.isArray(obj.links) ? obj.links.filter(isLink) : [];
  return {
    enabled: Boolean(obj.enabled) && links.length > 0,
    message: typeof obj.message === "string" ? obj.message : "",
    links,
  };
}

export async function loadDonations(url = assetUrl("donations.json")): Promise<DonationConfig> {
  try {
    const res = await fetch(url);
    if (!res.ok) return withQuietDonate({ enabled: false, message: "", links: [] });
    return withQuietDonate(normalizeDonations(await res.json()));
  } catch {
    return withQuietDonate({ enabled: false, message: "", links: [] });
  }
}
