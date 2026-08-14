# Reducer Pseudocode

```ts
type Event = {
  event_id: string
  ticket_id: string
  session_id: string
  timestamp: string
  event_type: string
  payload: Record<string, any>
  causation_id?: string
}

type State = {
  ticket_id: string
  generated_at: string
  latest_diagnosis?: Record<string, any>
  latest_action_plan?: Record<string, any>
  open_verification_items: string[]
  unresolved_escalations: Record<string, any>[]
  active_sessions: string[]
  touched_files: string[]
  last_sync_commit?: Record<string, any>
  last_review_result?: Record<string, any>
  timeout_risk_flag: boolean
  session_conflict_flag: boolean
}

const SESSION_TTL_MS = 30 * 60 * 1000 // M2: liveness TTL (default 30min, tune via config)

export function foldEvents(events: Event[]): State {
  const seen = new Set<string>()
  const lastEventAt = new Map<string, number>() // M2: TTL liveness, not membership
  const touchedFiles = new Set<string>()
  const openVerification = new Set<string>()
  const unresolvedEscalations: Record<string, any>[] = []

  let latestDiagnosis: Record<string, any> | undefined
  let latestActionPlan: Record<string, any> | undefined
  let lastSyncCommit: Record<string, any> | undefined
  let lastReviewResult: Record<string, any> | undefined
  let timeoutRisk = false

  // M4: epoch-normalized sort + event_id tiebreak => deterministic across mixed timezones
  const sorted = [...events].sort((a, b) =>
    (Date.parse(a.timestamp) - Date.parse(b.timestamp)) || a.event_id.localeCompare(b.event_id))

  for (const e of sorted) {
    if (seen.has(e.event_id)) continue
    seen.add(e.event_id)
    lastEventAt.set(e.session_id, Date.parse(e.timestamp)) // M2

    switch (e.event_type) {
      case 'diagnosis_recorded':
        latestDiagnosis = e.payload
        break
      case 'action_plan_recorded':
        latestActionPlan = e.payload
        break
      case 'file_touched':
        if (e.payload?.path) touchedFiles.add(e.payload.path)
        break
      case 'verification_planned':
        for (const item of e.payload?.items ?? []) openVerification.add(item)
        break
      case 'verification_result':
        for (const item of e.payload?.completed ?? []) openVerification.delete(item)
        break
      case 'escalation_suggested':
        unresolvedEscalations.push({ ...e.payload, event_id: e.event_id })
        break
      case 'escalation_resolved': { // M1: consume by causation_id
        const i = unresolvedEscalations.findIndex(x => x.event_id === e.causation_id)
        if (i >= 0) unresolvedEscalations.splice(i, 1)
        break
      }
      case 'review_gate_result':
        lastReviewResult = e.payload
        break
      case 'sync_commit_recorded':
        lastSyncCommit = e.payload
        break
      case 'session_closed':
        lastEventAt.delete(e.session_id)
        break
      case 'timeout_observed':
        timeoutRisk = true
        break
    }
  }

  return {
    ticket_id: sorted[0]?.ticket_id ?? 'UNKNOWN',
    generated_at: new Date().toISOString(),
    latest_diagnosis: latestDiagnosis,
    latest_action_plan: latestActionPlan,
    open_verification_items: [...openVerification],
    unresolved_escalations: unresolvedEscalations,
    active_sessions: [...lastEventAt.entries()].filter(([, t]) => Date.now() - t < SESSION_TTL_MS).map(([s]) => s), // M2
    touched_files: [...touchedFiles],
    last_sync_commit: lastSyncCommit,
    last_review_result: lastReviewResult,
    timeout_risk_flag: timeoutRisk,
    session_conflict_flag: [...lastEventAt.values()].filter(t => Date.now() - t < SESSION_TTL_MS).length > 1 // M2
  }
}
```
