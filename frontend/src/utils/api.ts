export const API_BASE_URL = "http://localhost:20001";

const buildHeader = (includeAuth: boolean = true): Record<string, string> => {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (includeAuth) {
    const token = localStorage.getItem("token");
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  return headers;
};

export const postJson = async <T>(
  path: string,
  payload: unknown,
  includeAuth: boolean = true,
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: buildHeader(includeAuth),
    body: JSON.stringify(payload),
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const msg = typeof data?.message === "string" ? data.message : "创建失败";
    throw new Error(msg);
  }

  return data as T;
};

export const getJson = async <T>(
  path: string,
  includeAuth: boolean = true,
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "GET",
    headers: buildHeader(includeAuth),
  });

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
  includeAuth: boolean = true,
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "PUT",
    headers: buildHeader(includeAuth),
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
  includeAuth: boolean = true,
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "DELETE",
    headers: buildHeader(includeAuth),
    body: payload ? JSON.stringify(payload) : undefined,
  });

  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    const msg = typeof data?.message === "string" ? data.message : "删除失败";
    throw new Error(msg);
  }

  return data as T;
};
