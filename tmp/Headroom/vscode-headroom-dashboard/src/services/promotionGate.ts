import type { PromotionGateResult, SourceDescriptor } from '../types/source.types';

export interface PromotionGateInput {
  source: SourceDescriptor;
  hasSchemaNote: boolean;
  eventCount: number;
  hasFallbackMapping: boolean;
  activeReadmeAligned: boolean;
  codegraphEnabled?: boolean;
  codegraphOk?: boolean;
}

export function evaluatePromotionGate(input: PromotionGateInput): PromotionGateResult {
  const blockers: string[] = [];
  const warnings: string[] = [];

  if (!input.source.available) {
    blockers.push('Metrics source endpoint is offline');
  }
  
  if (!input.hasSchemaNote) {
    blockers.push('Telemetry schema registration is pending promotion');
  }
  
  if (input.eventCount < 20) {
    blockers.push(`Metrics store contains ${input.eventCount}/20 events`);
  }
  
  if (!input.hasFallbackMapping) {
    blockers.push('Fallback level mapping definition is pending');
  }

  if (!input.activeReadmeAligned) {
    warnings.push('README doc sync is pending');
  }

  if (input.codegraphEnabled && !input.codegraphOk) {
    blockers.push('CodeGraph health check is failing');
  }

  const state = blockers.length > 0 ? 'blocked' : warnings.length > 0 ? 'hold' : 'pass';

  const evidence: Array<{ key: string; value: string | number | boolean | null }> = [
    { key: 'Active Contract', value: input.source.contract },
    { key: 'Promoted Status', value: input.source.promoted ? 'Yes' : 'No' },
    { key: 'Event Count', value: `${input.eventCount}/20` },
    { key: 'Schema Registered', value: input.hasSchemaNote ? 'Yes' : 'No' },
    { key: 'Fallback Defined', value: input.hasFallbackMapping ? 'Yes' : 'No' },
    { key: 'Docs Aligned', value: input.activeReadmeAligned ? 'Yes' : 'No' }
  ];

  if (input.codegraphEnabled) {
    evidence.push({ key: 'CodeGraph Online', value: input.codegraphOk ? 'Yes' : 'No' });
  }

  return {
    state,
    blockers,
    warnings,
    evidence
  };
}

