// src/authProvider.ts
import type { AuthProvider } from "react-admin";

const apiUrl = import.meta.env.VITE_API_URL;

export const authProvider: AuthProvider = {
  async login({ username, password }) {
    const res = await fetch(`${apiUrl}/auth/admin/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (!res.ok) throw new Error("Login failed");

    const data = await res.json();
    // Ví dụ data: { accessToken, role }
    localStorage.setItem("access_token", data.accessToken);
    localStorage.setItem("role", data.role ?? "admin");
  },

  async logout() {
    localStorage.removeItem("access_token");
    localStorage.removeItem("role");
  },

  async checkAuth() {
    const token = localStorage.getItem("access_token");
    if (!token) throw new Error("No token");
  },

  async checkError(error) {
    // Khi API trả 401/403, cho logout
    const status = error?.status;
    if (status === 401 || status === 403) {
      localStorage.removeItem("access_token");
      localStorage.removeItem("role");
      throw new Error("Unauthorized");
    }
  },

  async getPermissions() {
    return localStorage.getItem("role") || "admin";
  },
};
