export const COOLDOWN_MS = 60_000;

export type CooldownClock = () => number;

export function createCooldown(now: CooldownClock = () => Date.now()) {
  let lastAt = 0;
  let lastSearch: unknown = null;

  return {
    canAct(): boolean {
      return now() - lastAt >= COOLDOWN_MS;
    },
    mark(): void {
      lastAt = now();
    },
    cachedSearch<T>(fresh: T): T {
      if (now() - lastAt < COOLDOWN_MS && lastSearch != null) {
        return lastSearch as T;
      }
      lastSearch = fresh;
      lastAt = now();
      return fresh;
    },
    peekSearch<T>(): T | null {
      if (now() - lastAt < COOLDOWN_MS && lastSearch != null) {
        return lastSearch as T;
      }
      return null;
    },
  };
}
