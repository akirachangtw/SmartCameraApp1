"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.loadPilotConfig = loadPilotConfig;
exports.resolveHeadroomEndpoints = resolveHeadroomEndpoints;
exports.resolveSupervisorEndpoints = resolveSupervisorEndpoints;
exports.joinEndpoint = joinEndpoint;
exports.runConfigLoaderSelfTest = runConfigLoaderSelfTest;
const fs = __importStar(require("fs"));
const os = __importStar(require("os"));
const path = __importStar(require("path"));
function loadPilotConfig(configPath) {
    if (!configPath || !fs.existsSync(configPath))
        return {};
    return JSON.parse(fs.readFileSync(configPath, 'utf-8'));
}
function resolveHeadroomEndpoints(config) {
    return {
        proxyUrl: config.headroom?.proxy_url ?? config.sidecarBaseUrl ?? null,
        healthPath: config.headroom?.health_path ?? '/health',
        statsPath: config.headroom?.stats_path ?? '/stats'
    };
}
function resolveSupervisorEndpoints(config) {
    return {
        enabled: config.supervisor?.enabled ?? false,
        baseUrl: config.supervisor?.base_url ?? null,
        healthPath: config.supervisor?.health_path ?? '/health',
        ingestPath: config.supervisor?.ingest_path ?? '/events/ingest',
        summaryPath: config.supervisor?.summary_path ?? '/summary',
        recentPath: config.supervisor?.recent_path ?? '/events/recent',
        sourceStatus: config.supervisor?.source_status ?? 'prototype',
        promoted: config.supervisor?.promoted ?? false
    };
}
function joinEndpoint(baseUrl, endpointPath) {
    return `${baseUrl.replace(/\/$/, '')}/${endpointPath.replace(/^\//, '')}`;
}
function runConfigLoaderSelfTest() {
    const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'headroom-config-'));
    const configPath = path.join(tempDir, 'pilot.config.json');
    try {
        fs.writeFileSync(configPath, JSON.stringify({ version: 2, supervisor: { enabled: true, base_url: 'http://127.0.0.1:18788' } }));
        const first = resolveSupervisorEndpoints(loadPilotConfig(configPath));
        fs.writeFileSync(configPath, JSON.stringify({ version: 2, supervisor: { enabled: true, base_url: 'http://127.0.0.1:28788' } }));
        const second = resolveSupervisorEndpoints(loadPilotConfig(configPath));
        if (first.baseUrl === second.baseUrl || second.baseUrl !== 'http://127.0.0.1:28788')
            throw new Error('config hot reload failed');
        const isolated = resolveSupervisorEndpoints({ codegraph: { health_url: 'http://127.0.0.1:9999/health' } });
        if (isolated.baseUrl !== null)
            throw new Error('supervisor URL must not derive from codegraph.health_url');
    }
    finally {
        fs.rmSync(tempDir, { recursive: true, force: true });
    }
}
//# sourceMappingURL=configLoader.js.map