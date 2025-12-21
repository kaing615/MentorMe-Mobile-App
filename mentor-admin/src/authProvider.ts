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

    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: "Login failed" }));
      throw new Error(error.message || "Login failed");
    }

    const response = await res.json();
    // Backend trả về: { success: true, data: { accessToken, role, userId, email }, message: "..." }
    const data = response.data || response;
    
    if (!data.accessToken) {
      throw new Error("No access token received");
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
