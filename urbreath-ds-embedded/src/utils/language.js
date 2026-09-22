const LANGUAGE_CODE_PATTERN = /^[a-z]{2}$/;

export function normalizeLanguage(language) {
  if (typeof language !== "string") {
    return null;
  }

  const languageCode = language.trim().toLowerCase().split(/[-_]/)[0];
  return LANGUAGE_CODE_PATTERN.test(languageCode) ? languageCode : null;
}
