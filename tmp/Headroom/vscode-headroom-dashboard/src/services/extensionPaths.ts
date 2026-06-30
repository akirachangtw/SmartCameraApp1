import * as path from 'path';
import * as vscode from 'vscode';

export interface PilotPathOptions {
  workspaceRoot?: string;
  relativePilotConfigPath?: string;
}

export function resolvePilotConfigPath(
  context: vscode.ExtensionContext,
  options: PilotPathOptions = {}
): string {
  const configured = options.relativePilotConfigPath ?? '.agents/pilot/pilot.config.json';
  return path.join(options.workspaceRoot ?? context.extensionPath, configured);
}
