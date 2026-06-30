import * as vscode from 'vscode';
import type { HealthState, PilotMode } from '../types/dashboard.types';

export interface StatusBarState {
  mode: PilotMode;
  health: HealthState;
  note?: string;
}

export class StatusBarController {
  private readonly item: vscode.StatusBarItem;

  constructor() {
    this.item = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
    this.item.command = 'headroomDashboard.refresh';
    this.item.tooltip = 'Open Headroom pilot dashboard';
  }

  public show(state: StatusBarState): void {
    const icon = state.health === 'healthy' ? '$(pass-filled)' : state.health === 'degraded' ? '$(warning)' : state.health === 'offline' ? '$(error)' : '$(question)';
    const suffix = state.note ? ` · ${state.note}` : '';
    this.item.text = `${icon} Pilot: ${state.mode} / ${state.health}${suffix}`;
    this.item.show();
  }

  public hide(): void {
    this.item.hide();
  }

  public dispose(): void {
    this.item.dispose();
  }
}
