import { db } from '../db/db';
import api from '../api/axios';

class SyncManagerService {
  constructor() {
    this._syncing = false;
  }

  async syncUnsyncedDreams() {
    if (this._syncing) return;
    this._syncing = true;

    try {
      const unsyncedDreams = await db.local_dreams
        .where('is_synced').equals(0)
        .toArray();

      if (unsyncedDreams.length === 0) {
        console.log('[SyncManager] No unsynced dreams.');
        return;
      }

      console.log(`[SyncManager] Syncing ${unsyncedDreams.length} dreams...`);

      // Map local records to backend CreateDreamRequest contract
      const dreamsPayload = unsyncedDreams.map(d => ({
        clientId: d.clientId,
        content: d.text,
        dreamDate: d.date.split('T')[0] // LocalDate format: YYYY-MM-DD
      }));

      // SyncRequest contract: { dreams: [...], activities: [...] }
      const response = await api.post('/sync', {
        dreams: dreamsPayload,
        activities: [] // No activities tracked in the current UI yet
      });

      const { syncedDreams } = response.data;

      if (syncedDreams && syncedDreams.length > 0) {
        // Update local DB based on per-item results from backend
        const successClientIds = syncedDreams
          .filter(r => r.success)
          .map(r => r.clientId);

        if (successClientIds.length > 0) {
          // Find local IDs by clientId and mark them synced
          const localDreams = await db.local_dreams
            .where('clientId')
            .anyOf(successClientIds)
            .toArray();

          await db.local_dreams.bulkUpdate(
            localDreams.map(d => ({
              key: d.id,
              changes: { is_synced: 1 }
            }))
          );

          console.log(`[SyncManager] Successfully synced ${successClientIds.length} dreams.`);
        }

        const failures = syncedDreams.filter(r => !r.success);
        if (failures.length > 0) {
          console.warn('[SyncManager] Some dreams failed to sync:', failures);
        }
      }

    } catch (error) {
      console.error('[SyncManager] Sync error:', error);
    } finally {
      this._syncing = false;
    }
  }

  initBackgroundSync() {
    // Trigger sync when coming back online
    window.addEventListener('online', () => {
      console.log('[SyncManager] Device is online. Starting sync...');
      this.syncUnsyncedDreams();
    });

    // Periodic sync every 30 seconds when online
    setInterval(() => {
      if (navigator.onLine) {
        this.syncUnsyncedDreams();
      }
    }, 30000);

    // Initial sync on boot
    if (navigator.onLine) {
      // Small delay to let auth finish
      setTimeout(() => this.syncUnsyncedDreams(), 2000);
    }
  }
}

export const SyncManager = new SyncManagerService();
