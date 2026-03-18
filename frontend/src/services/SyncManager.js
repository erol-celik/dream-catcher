import { db } from '../db/db';
import { liveQuery } from 'dexie';
import api from '../api/axios';

class SyncManagerService {
  constructor() {
    this._syncing = false;
    this._debounceTimer = null;
    this._subscription = null;
    this.analysisListeners = [];
  }

  onAnalysisTriggered(callback) {
    this.analysisListeners.push(callback);
  }

  async syncUnsyncedDreams() {
    if (this._syncing) return;
    this._syncing = true;

    try {
      const unsyncedRecords = await db.local_dreams
        .where('is_synced').equals(0)
        .toArray();

      if (unsyncedRecords.length === 0) {
        console.log('[SyncManager] No unsynced records.');
        return;
      }

      console.log(`[SyncManager] Syncing ${unsyncedRecords.length} records...`);

      const dreamsPayload = [];
      const activitiesPayload = [];

      unsyncedRecords.forEach(d => {
        let localDateStr = d.date;
        if (d.date && d.date.endsWith('Z')) {
          const tzOffset = (new Date()).getTimezoneOffset() * 60000;
          localDateStr = new Date(new Date(d.date).getTime() - tzOffset).toISOString().slice(0, 19);
        }
        
        if (d.type === 'DUMMY_FOCUS') {
          activitiesPayload.push({
            clientId: d.clientId,
            activityType: 'DUMMY_FOCUS',
            activityDate: localDateStr.split('T')[0]
          });
        } else {
          dreamsPayload.push({
            clientId: d.clientId,
            content: d.text,
            sentiment: d.sentiment,
            dreamDate: localDateStr.split('T')[0]
          });
        }
      });

      const response = await api.post('/sync', {
        dreams: dreamsPayload,
        activities: activitiesPayload
      });

      const { syncedDreams, syncedActivities, newAnalysisTriggered } = response.data;

      if (newAnalysisTriggered) {
        console.log('[SyncManager] New Analysis Triggered! Notifying UI...');
        this.analysisListeners.forEach(cb => cb());
      }

      const allSynced = [...(syncedDreams || []), ...(syncedActivities || [])];

      if (allSynced.length > 0) {
        const successClientIds = allSynced
          .filter(r => r.success)
          .map(r => r.clientId);

        if (successClientIds.length > 0) {
          const localRecords = await db.local_dreams
            .where('clientId')
            .anyOf(successClientIds)
            .toArray();

          await db.local_dreams.bulkUpdate(
            localRecords.map(d => ({
              key: d.id,
              changes: { is_synced: 1 }
            }))
          );

          console.log(`[SyncManager] Successfully synced ${successClientIds.length} records.`);
        }
      }

    } catch (error) {
      console.error('[SyncManager] Sync error:', error);
    } finally {
      this._syncing = false;
    }
  }

  /**
   * Debounced sync trigger — prevents rapid-fire syncs when
   * multiple records are written in quick succession.
   */
  _debouncedSync() {
    if (this._debounceTimer) clearTimeout(this._debounceTimer);
    this._debounceTimer = setTimeout(() => {
      if (navigator.onLine) {
        this.syncUnsyncedDreams();
      }
    }, 1500);
  }

  initBackgroundSync() {
    // 1. Dexie liveQuery: triggers sync when unsynced records appear
    const observable = liveQuery(() =>
      db.local_dreams.where('is_synced').equals(0).count()
    );
    this._subscription = observable.subscribe({
      next: (unsyncedCount) => {
        if (unsyncedCount > 0) {
          console.log(`[SyncManager] ${unsyncedCount} unsynced record(s) detected.`);
          this._debouncedSync();
        }
      },
      error: (err) => console.error('[SyncManager] liveQuery error:', err)
    });

    // 2. Visibility API: sync when tab comes back to foreground
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible' && navigator.onLine) {
        console.log('[SyncManager] Tab visible. Triggering sync...');
        this._debouncedSync();
      }
    });

    // 3. Online event: sync when network is restored
    window.addEventListener('online', () => {
      console.log('[SyncManager] Device is online. Starting sync...');
      this.syncUnsyncedDreams();
    });

    // 4. Initial sync on boot (small delay to let auth finish)
    if (navigator.onLine) {
      setTimeout(() => this.syncUnsyncedDreams(), 2000);
    }
  }

  /**
   * Cleanup subscriptions — call when unmounting the app.
   */
  destroy() {
    if (this._subscription) {
      this._subscription.unsubscribe();
      this._subscription = null;
    }
    if (this._debounceTimer) {
      clearTimeout(this._debounceTimer);
    }
  }
}

export const SyncManager = new SyncManagerService();

