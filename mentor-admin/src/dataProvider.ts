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

// dataProvider custom: override riêng getList cho "admin/payouts" và "bookings"
export const dataProvider: DataProvider = {
  ...baseProvider,

  async getList(resource, params) {
    // 👉 Resource bookings: handle custom response format
    if (resource === "bookings") {
      const { filter = {}, pagination, sort } = params;
      const { page = 1, perPage = 10 } = pagination || {};

      const query: Record<string, string> = {
        page: String(page),
        limit: String(perPage),
      };

      // Add filters
      Object.entries(filter).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") {
          query[key] = String(value);
        }
      });

      const searchParams = new URLSearchParams(query);
      const url = `${apiUrl}/bookings?${searchParams.toString()}`;

      const { json } = await httpClient(url);

      // Backend returns: { success, message, data: { bookings, total, page, totalPages } }
      const bookings = json?.data?.bookings ?? [];
      const total = json?.data?.total ?? 0;

      // Ensure each item has id field
      const data = bookings.map((item: any) => ({
        ...item,
        id: item.id ?? item._id,
      }));

      return { data, total };
    }

    // 👉 Resource admin/payouts: gọi API payout custom
    if (resource === "admin/payouts") {
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
    }

    // Other resources use simpleRestProvider
    return baseProvider.getList(resource, params);
  },
};
