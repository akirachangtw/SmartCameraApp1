import type { SourceDescriptor } from '../types/source.types';
import type { SidecarSummary } from '../types/dashboard.types';

export interface FetchResult<T> { ok: boolean; status: number | null; data: T | null; error?: string }
export type FetchImplementation = (input: string | URL | Request, init?: RequestInit) => Promise<Response>;

export async function fetchWithTimeout<T>(url: string, timeoutMs = 5000, fetchImpl: FetchImplementation = fetch): Promise<FetchResult<T>> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const response = await fetchImpl(url, { signal: controller.signal });
    const text = await response.text();
    let data: T | null = null;
    try {
      data = text ? JSON.parse(text) as T : null;
    } catch {
      return { ok: false, status: response.status, data: null, error: 'non_json_response' };
    }
    return response.ok
      ? { ok: true, status: response.status, data }
      : { ok: false, status: response.status, data, error: `HTTP ${response.status}` };
  } catch (error) {
    return { ok: false, status: null, data: null, error: error instanceof Error ? error.message : 'Unknown fetch error' };
  } finally {
    clearTimeout(timer);
  }
}

export async function fetchHeadroomPair<THealth, TStats>(source: SourceDescriptor, healthUrl: string, statsUrl: string, timeoutMs = 5000) {
  const [health, stats] = await Promise.all([fetchWithTimeout<THealth>(healthUrl, timeoutMs), fetchWithTimeout<TStats>(statsUrl, timeoutMs)]);
  return { source, health, stats };
}

export async function fetchSidecarSummary(summaryUrl: string, timeoutMs = 3000): Promise<FetchResult<SidecarSummary>> {
  return fetchWithTimeout<SidecarSummary>(summaryUrl, timeoutMs);
}

export async function runDataFetcherSelfTest(): Promise<void> {
  const http500 = await fetchWithTimeout('http://test/500', 50, async () => new Response('{"error":"x"}', { status: 500 }));
  if (http500.ok || http500.status !== 500 || http500.error !== 'HTTP 500') throw new Error('HTTP 500 handling failed');
  const nonJson = await fetchWithTimeout('http://test/text', 50, async () => new Response('not-json', { status: 200 }));
  if (nonJson.ok || nonJson.error !== 'non_json_response') throw new Error('non-JSON handling failed');
  const timeout = await fetchWithTimeout('http://test/timeout', 5, async (_input, init) => new Promise<Response>((_resolve, reject) => {
    init?.signal?.addEventListener('abort', () => reject(new Error('aborted_by_timeout')));
  }));
  if (timeout.ok || timeout.error !== 'aborted_by_timeout') throw new Error('timeout handling failed');
}
