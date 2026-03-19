export const CONJUNCTIONS_AND_VERBS = [
  "ve", "bir", "sonra", "ben", "çok", "gibi", "gördüm", "and", "the", "was", "saw",
  "daha", "kadar", "ama", "için", "olarak", "olan", "ile", "değil", "var", "göre",
  "bana", "beni", "büyük", "iyi", "yeni", "yok", "zaman", "sadece", "başka", "hemen", 
  "oldu", "olacak", "yap", "yaptı", "geldi", "gitti", "aldı", "verdi", "dedi", "baktı",
  "kalktı", "uyandım", "rüyamda", "rüya", "dream", "woke", "went", "did", "said", "looked"
];

export class InputValidator {
  /**
   * Validates the dream text through a 4-layer heuristic check.
   * returning an object: { isValid: boolean, message?: string }
   */
  static validateDreamInput(text) {
    if (!text || text.trim().length === 0) {
      return { isValid: false, message: "Please write your dream." };
    }

    const cleanText = text.trim().toLowerCase();

    // Layer 1: Vowel Ratio
    // Valid text shouldn't be only consonants or only vowels. (Acceptable range roughly 15% - 70%)
    const vowels = cleanText.match(/[aeiouöüıi]/g);
    const letters = cleanText.match(/[a-zçşğüöı]/g);
    if (letters && letters.length > 5) {
      const vowelCount = vowels ? vowels.length : 0;
      const ratio = vowelCount / letters.length;
      if (ratio < 0.10 || ratio > 0.80) {
        return this._rejection();
      }
    }

    // Layer 2: Keyboard Mashing (lazy swipes)
    // Common keyboard smashes
    const mashingPattern = /(asdf|qwer|zxcv|1234|hjkl|tyui|ghjk)/i;
    if (mashingPattern.test(cleanText)) {
      return this._rejection();
    }

    // --- Punctuation Stripping (Layer 3 & 4 Prep) ---
    // Strip punctuation so only alphanumeric characters and spaces remain
    const textWithoutPunctuation = cleanText
      .replace(/[.,\/#!$%\^&\*;:{}=\-_`~()]/g, "")
      .replace(/\s{2,}/g, " ");

    // Layer 3: Word Length Anomaly 
    // Any single word without spaces longer than 20 chars is likely gibberish
    const words = textWithoutPunctuation.split(/\s+/);
    for (let word of words) {
      if (word.length > 20) {
        return this._rejection();
      }
    }

    // Layer 4: Mini-Dictionary Intersection
    // At least 2 common storytelling words must be present for a narrative
    // Using Regex word boundaries to prevent false negatives from punctuation/formatting
    const dictionaryRegex = new RegExp("\\b(" + CONJUNCTIONS_AND_VERBS.join("|") + ")\\b", "gi");
    const matches = textWithoutPunctuation.match(dictionaryRegex);

    if (!matches || matches.length < 2) {
      return this._rejection();
    }

    return { isValid: true };
  }

  static _rejection() {
    return { 
      isValid: false, 
      message: "Our subconscious network couldn't decode these words. Could you describe your dream with a bit more detail?" 
    };
  }
}
