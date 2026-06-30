export type RiskId = 'retrieval-drift' | 'compression-harm' | 'observability-gap';
export type RiskSeverity = 'info' | 'warn' | 'critical';

export interface RiskEvidence {
  key: string;
  value: string | number | boolean | null;
  note?: string;
}

export interface RiskSignal {
  id: RiskId;
  title: string;
  severity: RiskSeverity;
  triggered: boolean;
  reasons: string[];
  affectedPanels: string[];
  fallbackHint?: string;
  evidence: RiskEvidence[];
}
