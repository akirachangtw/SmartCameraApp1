import type { PromotionGateResult, SourceDescriptor } from '../types/source.types';
import { runConfigLoaderSelfTest } from './configLoader';
import { runDataFetcherSelfTest } from './dataFetcher';

export interface PromotionGateInput {
  source: SourceDescriptor;
  hasSchemaNote: boolean;
  realEventCount: number;
  hasFallbackMapping: boolean;
  activeReadmeAligned: boolean;
  codegraphEnabled?: boolean;
  codegraphOk?: boolean;
}

export function evaluatePromotionGate(input: PromotionGateInput): PromotionGateResult {
  const blockers: string[] = [];
  const warnings: string[] = [];
  if (!input.source.available) blockers.push('Metrics source endpoint is offline');
  if (input.source.sourceStatus !== 'promoted') blockers.push('Metrics source remains prototype');
  if (!input.source.promoted) blockers.push('Metrics source is not promoted');
  if (!input.hasSchemaNote) blockers.push('Telemetry schema registration is pending promotion');
  if (input.realEventCount < 20) blockers.push(`Real evidence contains ${input.realEventCount}/20 events`);
  if (!input.hasFallbackMapping) blockers.push('Fallback level mapping definition is pending');
  if (!input.activeReadmeAligned) warnings.push('README doc sync is pending');
  if (input.codegraphEnabled && !input.codegraphOk) blockers.push('CodeGraph health check is failing');
  const state = blockers.length > 0 ? 'blocked' : warnings.length > 0 ? 'hold' : 'pass';
  return {
    state,
    blockers,
    warnings,
    evidence: [
      { key: 'Active Contract', value: input.source.contract },
      { key: 'Source Status', value: input.source.sourceStatus ?? 'unknown' },
      { key: 'Promoted Status', value: input.source.promoted ? 'Yes' : 'No' },
      { key: 'Real Event Count', value: `${input.realEventCount}/20` },
      { key: 'Schema Registered', value: input.hasSchemaNote ? 'Yes' : 'No' },
      { key: 'Fallback Defined', value: input.hasFallbackMapping ? 'Yes' : 'No' },
      { key: 'Docs Aligned', value: input.activeReadmeAligned ? 'Yes' : 'No' }
    ]
  };
}

export async function runDashboardContractSelfTest(): Promise<void> {
  runConfigLoaderSelfTest();
  await runDataFetcherSelfTest();
  const gate = evaluatePromotionGate({
    source: { contract: 'jsonlPilotContract', label: 'Preview', available: true, promoted: false, sourceStatus: 'prototype' },
    hasSchemaNote: true,
    realEventCount: 0,
    hasFallbackMapping: true,
    activeReadmeAligned: true
  });
  if (gate.state === 'pass') throw new Error('fixture-only prototype source must not pass');
}
