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
    const status = error?.status || error?.response?.status;
    
    // 401 Unauthorized - token invalid/expired, need to logout
    if (status === 401) {
      localStorage.removeItem("access_token");
      localStorage.removeItem("role");
      localStorage.removeItem("userId");
      localStorage.removeItem("user_email");
      return Promise.reject();
    }
    
    // 403 Forbidden - token valid but no permission, don't logout
    // Just show error message to user
    if (status === 403) {
      return Promise.resolve(); // Don't throw, just let the component handle it
    }
  },

  async getPermissions() {
    return localStorage.getItem("role") || "admin";
  },
};
