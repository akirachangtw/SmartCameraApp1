export type TokenHistory = number[]

declare module 'claude-code' {
  interface PluginState {
    'token-weather': { history: TokenHistory }
  }
}
