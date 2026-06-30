import * as vscode from 'vscode';
import { HeadroomViewProvider } from './headroomViewProvider';
import { StatusBarController } from './ui/statusBarController';
import type { HealthState, PilotMode } from './types/dashboard.types';

export function activate(context: vscode.ExtensionContext): void {
  const statusBarController = new StatusBarController();

  const output = vscode.window.createOutputChannel('Headroom Pilot');
  const provider = new HeadroomViewProvider(context.extensionUri, {
    workspaceRoot: vscode.workspace.workspaceFolders?.[0]?.uri.fsPath,
    output,
    context,
    onStatusChange: (state) => {
      statusBarController.show({
        mode: (state.mode as PilotMode) || 'pilot',
        health: state.level as HealthState,
        note: state.label
      });
    }
  });

  context.subscriptions.push(
    vscode.window.registerWebviewViewProvider('headroomDashboard.view', provider)
  );

  context.subscriptions.push(
    vscode.commands.registerCommand('headroomDashboard.refresh', async () => {
      await provider.refresh();
    })
  );

  context.subscriptions.push(
    vscode.commands.registerCommand('headroomDashboard.openConfig', async () => {
      const configUri = provider.getConfigUri();
      if (!configUri) {
        vscode.window.showWarningMessage('No workspace pilot config found.');
        return;
      }
      const doc = await vscode.workspace.openTextDocument(configUri);
      await vscode.window.showTextDocument(doc);
    })
  );

  context.subscriptions.push(statusBarController, output);
}

export function deactivate(): void {}
