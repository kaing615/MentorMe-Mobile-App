import axios from "axios";

const http = axios.create({
  baseURL: process.env.API_BASE_URL ?? "http://localhost:4000/api/v1",
  timeout: 10_000,
  headers: { Accept: "application/json" },
});

export function setAuthToken(token?: string) {
  if (token) http.defaults.headers.common.Authorization = `Bearer ${token}`;
  else delete http.defaults.headers.common.Authorization;
}

http.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err?.response?.status;
    const data = err?.response?.data;
    const message = err?.message ?? "Request failed";
    return Promise.reject({
      status,
      data,
      message,
      url: err?.config?.url,
      method: err?.config?.method,
    });
  }
);

export async function get<T = unknown>(
  url: string,
  config?: Parameters<typeof http.get>[1]
): Promise<T> {
  const { data } = await http.get<T>(url, config);
  return data;
}

export async function post<T = unknown, B = unknown>(
  url: string,
  body?: B,
  config?: Parameters<typeof http.post>[2]
): Promise<T> {
  const { data } = await http.post<T>(url, body, config);
  return data;
}

export async function put<T = unknown, B = unknown>(
  url: string,
  body?: B,
  config?: Parameters<typeof http.put>[2]
): Promise<T> {
  const { data } = await http.put<T>(url, body, config);
  return data;
}

export async function del<T = unknown>(
  url: string,
  config?: Parameters<typeof http.delete>[1]
): Promise<T> {
  const { data } = await http.delete<T>(url, config);
  return data;
}

export default { http, setAuthToken, get, post, put, del };
