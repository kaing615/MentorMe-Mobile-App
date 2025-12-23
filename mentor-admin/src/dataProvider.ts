// src/dataProvider.ts
import simpleRestProvider from "ra-data-simple-rest";
import { fetchUtils, DataProvider } from "react-admin";

const apiUrl = import.meta.env.VITE_API_URL;

// httpClient cũ (giữ nguyên)
const httpClient = (url: string, options: fetchUtils.Options = {}) => {
  const token = localStorage.getItem("access_token");
  options.user = {
    authenticated: true,
    token: token ? `Bearer ${token}` : "",
  };
  return fetchUtils.fetchJson(url, options);
};

// base provider cho các resource bình thường (users, bookings, reports, ...)
const baseProvider = simpleRestProvider(apiUrl, httpClient) as DataProvider;

// dataProvider custom: override riêng getList cho "admin/payouts"
export const dataProvider: DataProvider = {
  ...baseProvider,

  async getList(resource, params) {
    // Các resource khác dùng simpleRestProvider như cũ
    if (resource !== "admin/payouts") {
      return baseProvider.getList(resource, params);
    }

    // 👉 Resource admin/payouts: gọi API payout custom
    const { filter = {}, pagination } = params;
    const perPage = pagination?.perPage ?? 25;

    const query: Record<string, string> = {};

    // map filter -> query (?status=..., ?mentorId=...)
    Object.entries(filter).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        query[key] = String(value);
      }
    });

    // limit cho BE
    query["limit"] = String(perPage);

    const searchParams = new URLSearchParams(query);
    const url = `${apiUrl}/admin/payouts?${searchParams.toString()}`;

    const { json } = await httpClient(url);

    const items = json?.data?.items ?? [];

    // đảm bảo mỗi item có field id cho React Admin
    const data = items.map((item: any) => ({
      ...item,
      id: item.id ?? item._id,
    }));

    // simpleRestProvider cần { data, total }
    return {
      data,
      total: data.length, // demo: dùng length, chưa cần total thật
    };
  },
};
