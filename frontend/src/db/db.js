import Dexie from 'dexie';

export const db = new Dexie('DreamCatcherDB');

db.version(2).stores({
  local_dreams: '++id, clientId, text, sentiment, is_synced, date'
});

db.version(3).stores({
  local_dreams: '++id, clientId, text, sentiment, is_synced, date, type',
  seen_flashcards: 'flashcardId, seenAt'
});
