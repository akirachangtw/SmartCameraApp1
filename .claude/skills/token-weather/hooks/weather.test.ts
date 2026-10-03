import { test, expect } from 'claude-code/testing'
import { fmtDelta, sparkline, weatherFor } from './weather'

test('maps boundaries', () => {
  expect(weatherFor(24).label).toBe('Clear')
  expect(weatherFor(25).label).toBe('Cloudy')
  expect(weatherFor(49).label).toBe('Cloudy')
  expect(weatherFor(50).label).toBe('Showers')
  expect(weatherFor(74).label).toBe('Showers')
  expect(weatherFor(75).label).toBe('Storm')
  expect(weatherFor(89).label).toBe('Storm')
  expect(weatherFor(90).label).toBe('Compact soon')
})
test('delta and sparkline', () => {
  expect(fmtDelta([1000, 3500])).toBe('+2.5k')
  expect(fmtDelta([3500, 1000])).toBe('−2.5k')
  expect(fmtDelta([5])).toBe('—')
  expect(sparkline([0, 100], 100)).toBe('▁█')
})
