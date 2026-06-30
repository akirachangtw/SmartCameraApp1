import type { MetricValue, PromotionState } from './dashboard.types';

export type PanelId = 'session' | 'retrieval' | 'compression' | 'latency' | 'quality' | 'governance' | 'headroom-health' | 'headroom-stats';
export type PanelRenderState = 'active' | 'prototype' | 'blocked' | 'unavailable';

export interface PanelViewModel {
  id: PanelId;
  title: string;
  renderState: PanelRenderState;
  promotionState: PromotionState;
  sourceLabel: string;
  description?: string;
  metrics: MetricValue[];
  blockers?: string[];
}
