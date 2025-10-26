export const detectEnglishText = (input: string, threshold: number = 0.6): boolean => {
  // Remove common zero-width characters that sneak in from copy/paste
  // eslint-disable-next-line no-misleading-character-class
  const cleaned = input.replace(/[\u200B\u200C\u200D\uFEFF\u2060]/g, '');

  // Prefer Unicode property escapes when available
  try {
    const reLetter = /\p{Letter}/u;
    const reLatin  = /\p{Script=Latin}/u;

    let totalLetters = 0;
    let latinLetters = 0;

    for (const ch of cleaned) {
      if (!reLetter.test(ch)) continue;
      totalLetters++;
      if (reLatin.test(ch)) latinLetters++;
    }

    if (!totalLetters) return false; // no letters to judge
    const englishRatio = latinLetters / totalLetters;
    return englishRatio >= threshold;
  } catch {
    // Fallback for environments without Unicode property escapes
    const reLetterBasic = /[A-Za-z\u00C0-\u024F\u0E00-\u0E7F]/; // Latin + Thai (basic heuristic)
    const reLatinBasic  = /[A-Za-z\u00C0-\u024F]/;

    let totalLetters = 0;
    let latinLetters = 0;

    for (const ch of cleaned) {
      if (!reLetterBasic.test(ch)) continue;
      totalLetters++;
      if (reLatinBasic.test(ch)) latinLetters++;
    }

    if (!totalLetters) return false;
    const englishRatio = latinLetters / totalLetters;
    return englishRatio >= threshold;
  }
}
