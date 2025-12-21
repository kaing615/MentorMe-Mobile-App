// src/dataProvider.ts
import simpleRestProvider from "ra-data-simple-rest";
import { fetchUtils } from "react-admin";

const apiUrl = import.meta.env.VITE_API_URL;

const httpClient = (url: string, options: fetchUtils.Options = {}) => {
  const token = localStorage.getItem("access_token");
  options.user = {
    authenticated: true,
    token: token ? `Bearer ${token}` : "",
  };
  return fetchUtils.fetchJson(url, options);
};

export const dataProvider = simpleRestProvider(apiUrl, httpClient);
