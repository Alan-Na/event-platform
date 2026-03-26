const TOKEN_KEY = 'eventflow_token';
const LANGUAGE_KEY = 'app_language';

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearStoredToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getStoredLanguage() {
  return localStorage.getItem(LANGUAGE_KEY) || 'en';
}

export function setStoredLanguage(language: string) {
  localStorage.setItem(LANGUAGE_KEY, language);
}
