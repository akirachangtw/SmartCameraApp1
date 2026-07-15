export type ContractKind = 'activeHeadroomContract' | 'jsonlPilotContract' | 'futurePromotedContract';
export type GateState = 'pass' | 'hold' | 'blocked';

export interface SourceDescriptor {
  contract: ContractKind;
  label: string;
  available: boolean;
  promoted: boolean;
  sourceStatus?: 'prototype' | 'promoted' | 'unavailable';
  endpoint?: string | null;
  note?: string;
}

export interface PromotionGateResult {
  state: GateState;
  blockers: string[];
  warnings: string[];
  evidence: Array<{
    key: string;
    value: string | number | boolean | null;
  }>;
}
