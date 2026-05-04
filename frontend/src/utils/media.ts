export const resolveMediaUrl = (url?: string) => {
  if (!url) return ''

  const normalized = url.trim().replace(/\\/g, '/')
  if (!normalized) return ''

  if (/^(https?:)?\/\//i.test(normalized) || normalized.startsWith('data:') || normalized.startsWith('blob:')) {
    return normalized
  }

  if (normalized.startsWith('/api/')) {
    return normalized
  }

  if (normalized.startsWith('/uploads/')) {
    return `/api${normalized}`
  }

  if (normalized.startsWith('uploads/')) {
    return `/api/${normalized}`
  }

  return normalized
}
