import type { DonationConfig } from "./types";

export const VENMO_DONATE_URL = "https://venmo.com/code?user_id=1857304970395648420";

export const DONATE_LABEL = "Donate via Venmo";

export function withQuietDonate(config: DonationConfig): DonationConfig {
  const hasVenmo = config.links.some((link) => link.url === VENMO_DONATE_URL);
  const links = hasVenmo
    ? config.links.map((link) =>
        link.url === VENMO_DONATE_URL ? { ...link, label: DONATE_LABEL } : link,
      )
    : [...config.links, { label: DONATE_LABEL, url: VENMO_DONATE_URL }];
  return { enabled: true, message: config.message, links };
}
