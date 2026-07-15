"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.evaluatePromotionGate = evaluatePromotionGate;
exports.runDashboardContractSelfTest = runDashboardContractSelfTest;
const configLoader_1 = require("./configLoader");
const dataFetcher_1 = require("./dataFetcher");
function evaluatePromotionGate(input) {
    const blockers = [];
    const warnings = [];
    if (!input.source.available)
        blockers.push('Metrics source endpoint is offline');
    if (input.source.sourceStatus !== 'promoted')
        blockers.push('Metrics source remains prototype');
    if (!input.source.promoted)
        blockers.push('Metrics source is not promoted');
    if (!input.hasSchemaNote)
        blockers.push('Telemetry schema registration is pending promotion');
    if (input.realEventCount < 20)
        blockers.push(`Real evidence contains ${input.realEventCount}/20 events`);
    if (!input.hasFallbackMapping)
        blockers.push('Fallback level mapping definition is pending');
    if (!input.activeReadmeAligned)
        warnings.push('README doc sync is pending');
    if (input.codegraphEnabled && !input.codegraphOk)
        blockers.push('CodeGraph health check is failing');
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
async function runDashboardContractSelfTest() {
    (0, configLoader_1.runConfigLoaderSelfTest)();
    await (0, dataFetcher_1.runDataFetcherSelfTest)();
    const gate = evaluatePromotionGate({
        source: { contract: 'jsonlPilotContract', label: 'Preview', available: true, promoted: false, sourceStatus: 'prototype' },
        hasSchemaNote: true,
        realEventCount: 0,
        hasFallbackMapping: true,
        activeReadmeAligned: true
    });
    if (gate.state === 'pass')
        throw new Error('fixture-only prototype source must not pass');
}
//# sourceMappingURL=promotionGate.js.map