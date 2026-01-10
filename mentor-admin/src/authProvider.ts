import type { AuthProvider } from "react-admin";

const apiUrl = import.meta.env.VITE_API_URL;

export const authProvider: AuthProvider = {
  async login({ username, password }) {
    const res = await fetch(`${apiUrl}/auth/admin/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (!res.ok) {
      const error = await res
        .json()
        .catch(() => ({ message: "Đăng nhập thất bại" }));
      throw new Error(error.message || "Đăng nhập thất bại");
    }

    const response = await res.json();
    const data = response.data || response;

    if (!data.accessToken) {
      throw new Error("Không nhận được access token");
    }

    localStorage.setItem("access_token", data.accessToken);
    localStorage.setItem("role", data.role || "admin");
    localStorage.setItem("userId", data.userId || "");
    localStorage.setItem("user_email", data.email || "");

    return Promise.resolve();
  },

  async logout() {
    localStorage.removeItem("access_token");
    localStorage.removeItem("role");
    localStorage.removeItem("userId");
    localStorage.removeItem("user_email");
  },

  async checkAuth() {
    const token = localStorage.getItem("access_token");
    if (!token) throw new Error("Không có token");
  },

  async checkError(error) {
    const status = error?.status || error?.response?.status;
    if (status === 401) {
      localStorage.removeItem("access_token");
      localStorage.removeItem("role");
      localStorage.removeItem("userId");
      localStorage.removeItem("user_email");
      return Promise.reject();
    }

    if (status === 403) {
      return Promise.resolve();
    }
  },

  async getPermissions() {
    return localStorage.getItem("role") || "admin";
  },
};
