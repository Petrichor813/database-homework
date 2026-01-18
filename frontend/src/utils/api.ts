export const API_BASE_URL = 'http://localhost:8080'

export const postJson = async <T>(path: string, payload: unknown): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  })

  const data = await response.json().catch(() => ({}))

  if (!response.ok) {
    const message = typeof data?.message === 'string' ? data.message : '请求失败'
    throw new Error(message)
  }

  return data as T
}
