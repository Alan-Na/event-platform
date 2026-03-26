export function formatDateTime(value?: string, language = 'en') {
  if (!value) return '-';
  return new Intl.DateTimeFormat(language === 'zh-CN' ? 'zh-CN' : 'en-CA', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
}

export function formatDateRange(start?: string, end?: string, language = 'en') {
  if (!start || !end) return '-';
  return `${formatDateTime(start, language)} - ${formatDateTime(end, language)}`;
}
