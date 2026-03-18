import flashcardsData from '../constants/flashcards.json';

const FLASHCARDS = flashcardsData.flashcards;

export class FlashcardEngine {
  /**
   * Initialize with your Dexie LocalDb instance
   * @param {Object} db - Dexie DB instance 
   */
  constructor(db) {
    this.db = db; 
  }

  /**
   * Retrieves a random unread flashcard and marks it as seen in the local db.
   * If all cards have been seen, it resets the seen_flashcards table.
   */
  async getNextFlashcard() {
    try {
      // Get IDs of all previously seen flashcards
      const seenRecords = await this.db.seen_flashcards.toArray();
      const seenIds = new Set(seenRecords.map((record) => record.flashcardId));

      // Filter to find unseen flashcards
      let availableCards = FLASHCARDS.filter((card) => !seenIds.has(card.id));

      // If all have been seen, clear the seen_flashcards table to loop back.
      if (availableCards.length === 0) {
        await this.db.seen_flashcards.clear();
        availableCards = [...FLASHCARDS];
      }

      // Pick a random card from available
      const randomIndex = Math.floor(Math.random() * availableCards.length);
      const selectedCard = availableCards[randomIndex];

      // Mark the card as seen in the background asynchronously
      this._markAsSeen(selectedCard.id);

      return selectedCard;
    } catch (error) {
      console.error("Error retrieving next flashcard from DB. Returning fallback.", error);
      // Fallback in case of an issue
      return FLASHCARDS[Math.floor(Math.random() * FLASHCARDS.length)];
    }
  }

  async _markAsSeen(flashcardId) {
    try {
      await this.db.seen_flashcards.put({
        flashcardId,
        seenAt: new Date().toISOString()
      });
    } catch (error) {
      console.error("Flashcard Engine: Failed to mark flashcard as seen.", error);
    }
  }
}
