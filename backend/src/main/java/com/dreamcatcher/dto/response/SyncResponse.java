package com.dreamcatcher.dto.response;

import java.util.List;

/**
 * Response payload for bulk sync operations.
 * Reports per-item success/failure so the client can update
 * its local is_synced flags accordingly.
 */
public record SyncResponse(
        List<SyncedItemResult> syncedDreams,
        List<SyncedItemResult> syncedActivities,
        int totalSynced,
        int totalFailed,
        boolean newAnalysisTriggered
) {

    /**
     * Result of syncing a single item, keyed by client_id.
     */
    public record SyncedItemResult(
            String clientId,
            Long serverId,
            boolean success,
            String errorMessage
    ) {
        public static SyncedItemResult success(String clientId, Long serverId) {
            return new SyncedItemResult(clientId, serverId, true, null);
        }

        public static SyncedItemResult failure(String clientId, String errorMessage) {
            return new SyncedItemResult(clientId, null, false, errorMessage);
        }
    }

}
