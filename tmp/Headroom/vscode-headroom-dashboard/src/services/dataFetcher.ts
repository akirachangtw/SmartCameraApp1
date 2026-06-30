import type { SourceDescriptor } from '../types/source.types';
import type { SidecarSummary } from '../types/dashboard.types';

export interface FetchResult<T> {
  ok: boolean;
  status: number | null;
  data: T | null;
  error?: string;
}

export async function fetchWithTimeout<T>(url: string, timeoutMs = 5000): Promise<FetchResult<T>> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);

  try {
    const response = await fetch(url, { signal: controller.signal });
    const text = await response.text();
    let data: T | null;
    try {
      data = JSON.parse(text) as T;
    } catch {
      data = { raw: text } as T;
    }
    return { ok: response.ok, status: response.status, data };
  } catch (error) {
    return {
      ok: false,
      status: null,
      data: null,
      error: error instanceof Error ? error.message : 'Unknown fetch error'
    };
  } finally {
    clearTimeout(timer);
  }
}

export async function fetchHeadroomPair<THealth, TStats>(
  source: SourceDescriptor,
  healthUrl: string,
  statsUrl: string,
  timeoutMs = 5000
): Promise<{ source: SourceDescriptor; health: FetchResult<THealth>; stats: FetchResult<TStats> }> {
  const [health, stats] = await Promise.all([
    fetchWithTimeout<THealth>(healthUrl, timeoutMs),
    fetchWithTimeout<TStats>(statsUrl, timeoutMs)
  ]);

  return { source, health, stats };
}

export async function fetchSidecarSummary(
  summaryUrl: string,
  timeoutMs = 3000
): Promise<FetchResult<SidecarSummary>> {
  return fetchWithTimeout<SidecarSummary>(summaryUrl, timeoutMs);
}

