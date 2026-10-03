import { atom, read, update } from 'claude-code'
import type { Register } from 'claude-code'

import { MAX_TURNS, fmt, fmtDelta, sparkline, weatherFor } from './weather'

const history = atom({ plugin: 'token-weather', key: 'history' } as const, [] as number[])

export const register: Register = on => {
  on('turn.complete', async ($, e, next) => {
    const { context } = await $.session.usage()

    if (context.tokens !== undefined) {
      const tokens = context.tokens
      await update($, history, h => [...h, tokens].slice(-MAX_TURNS))
    }

    return next(e)
  })

  on('ui.render', { component: 'AbovePrompt' }, async ($, e, next) => {
    if (e.props.hasSurvey) return next(e)

    const { context } = await $.session.usage()
    const hist = await read($, history)
    const tokens = context.tokens ?? hist[hist.length - 1]

    if (tokens === undefined || !context.window) return next(e)

    const percent = context.percent ?? Math.round((tokens / context.window) * 100)
    const w = weatherFor(percent)
    const { Box, Text } = $.ui.resolve(e)

    return (
      <Box>
        <Text color={w.color}>
          {w.icon} {w.label} {percent}%
        </Text>
        <Text dimColor>
          {'  '}
          {fmt(tokens)}/{fmt(context.window)}
          {'  '}
          {sparkline(hist, context.window)}
          {'  Δ '}
          {fmtDelta(hist)}
        </Text>
      </Box>
    )
  })
}
