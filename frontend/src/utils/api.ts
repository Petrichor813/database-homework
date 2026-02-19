export const API_BASE_URL = "http://localhost:5200";

export const postJson = async <T>(
  path: string,
  payload: unknown,
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const msg = typeof data?.message === "string" ? data.message : "创建失败";
    throw new Error(msg);
  }

  return data as T;
};

export const getJson = async <T>(path: string): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`);

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const msg =
      typeof data?.message === "string" ? data.message : "获取数据失败";
    throw new Error(msg);
  }

  return data as T;
};

export const putJson = async <T>(
  path: string,
  payload: unknown,
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const msg = typeof data?.message === "string" ? data.message : "更新失败";
    throw new Error(msg);
  }

  return data as T;
};

export const deleteJson = async <T>(
  path: string,
  payload?: unknown,
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
    },
    body: payload ? JSON.stringify(payload) : undefined,
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const msg = typeof data?.message === "string" ? data.message : "删除失败";
    throw new Error(msg);
  }

  return data as T;
};
