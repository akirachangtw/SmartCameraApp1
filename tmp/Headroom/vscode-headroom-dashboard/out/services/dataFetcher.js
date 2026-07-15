"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.fetchWithTimeout = fetchWithTimeout;
exports.fetchHeadroomPair = fetchHeadroomPair;
exports.fetchSidecarSummary = fetchSidecarSummary;
exports.runDataFetcherSelfTest = runDataFetcherSelfTest;
async function fetchWithTimeout(url, timeoutMs = 5000, fetchImpl = fetch) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    try {
        const response = await fetchImpl(url, { signal: controller.signal });
        const text = await response.text();
        let data = null;
        try {
            data = text ? JSON.parse(text) : null;
        }
        catch {
            return { ok: false, status: response.status, data: null, error: 'non_json_response' };
        }
        return response.ok
            ? { ok: true, status: response.status, data }
            : { ok: false, status: response.status, data, error: `HTTP ${response.status}` };
    }
    catch (error) {
        return { ok: false, status: null, data: null, error: error instanceof Error ? error.message : 'Unknown fetch error' };
    }
    finally {
        clearTimeout(timer);
    }
}
async function fetchHeadroomPair(source, healthUrl, statsUrl, timeoutMs = 5000) {
    const [health, stats] = await Promise.all([fetchWithTimeout(healthUrl, timeoutMs), fetchWithTimeout(statsUrl, timeoutMs)]);
    return { source, health, stats };
}
async function fetchSidecarSummary(summaryUrl, timeoutMs = 3000) {
    return fetchWithTimeout(summaryUrl, timeoutMs);
}
async function runDataFetcherSelfTest() {
    const http500 = await fetchWithTimeout('http://test/500', 50, async () => new Response('{"error":"x"}', { status: 500 }));
    if (http500.ok || http500.status !== 500 || http500.error !== 'HTTP 500')
        throw new Error('HTTP 500 handling failed');
    const nonJson = await fetchWithTimeout('http://test/text', 50, async () => new Response('not-json', { status: 200 }));
    if (nonJson.ok || nonJson.error !== 'non_json_response')
        throw new Error('non-JSON handling failed');
    const timeout = await fetchWithTimeout('http://test/timeout', 5, async (_input, init) => new Promise((_resolve, reject) => {
        init?.signal?.addEventListener('abort', () => reject(new Error('aborted_by_timeout')));
    }));
    if (timeout.ok || timeout.error !== 'aborted_by_timeout')
        throw new Error('timeout handling failed');
}
//# sourceMappingURL=dataFetcher.js.map