export const MAX_TURNS = 12

export type Weather = { icon: string; label: string; color: string }

export function weatherFor(percent: number): Weather {
  if (percent < 25) return { icon: '☀', label: 'Clear', color: 'green' }
  if (percent < 50) return { icon: '☁', label: 'Cloudy', color: 'cyan' }
  if (percent < 75) return { icon: '☂', label: 'Showers', color: 'yellow' }
  if (percent < 90) return { icon: '☇', label: 'Storm', color: 'magenta' }
  return { icon: '↯', label: 'Compact soon', color: 'red' }
}

const BARS = '▁▂▃▄▅▆▇█'

export function sparkline(history: number[], window: number): string {
  return history
    .map(t => BARS[Math.min(7, Math.max(0, Math.floor((t / window) * 8)))])
    .join('')
}

export function fmt(n: number): string {
  return n >= 1000 ? `${(n / 1000).toFixed(1)}k` : String(n)
}

export function fmtDelta(history: number[]): string {
  if (history.length < 2) return '—'
  const d = history[history.length - 1] - history[history.length - 2]
  return `${d >= 0 ? '+' : '−'}${fmt(Math.abs(d))}`
}
