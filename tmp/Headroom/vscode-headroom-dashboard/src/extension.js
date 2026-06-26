const vscode = require('vscode');
const { HeadroomViewProvider } = require('./headroomViewProvider');

function activate(context) {
  const statusBar = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
  statusBar.command = 'headroomDashboard.refresh';
  statusBar.text = 'Headroom: Checking';
  statusBar.tooltip = 'Headroom pilot health';
  statusBar.show();

  const output = vscode.window.createOutputChannel('Headroom Pilot');
  const provider = new HeadroomViewProvider(context.extensionUri, {
    workspaceRoot: vscode.workspace.workspaceFolders?.[0]?.uri.fsPath,
    output,
    onStatusChange: (state) => {
      statusBar.text = `Headroom: ${state.label}`;
      statusBar.tooltip = state.detail;
    }
  });

  context.subscriptions.push(
    vscode.window.registerWebviewViewProvider('headroomDashboard.view', provider)
  );

  context.subscriptions.push(
    vscode.commands.registerCommand('headroomDashboard.refresh', () => {
      provider.manualRefresh();
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

  context.subscriptions.push(statusBar, output);
}

function deactivate() {}

module.exports = {
  activate,
  deactivate
};
