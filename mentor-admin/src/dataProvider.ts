// src/dataProvider.ts
import simpleRestProvider from "ra-data-simple-rest";
import { DataProvider, fetchUtils } from "react-admin";

const apiUrl = import.meta.env.VITE_API_URL;

const normalizeId = (item: any) => ({
  ...item,
  id: item.id ?? item._id,
});

const toQueryValue = (value: any) => {
  if (value instanceof Date) return value.toISOString();
  return String(value);
};

// httpClient with better error handling
const httpClient = async (url: string, options: fetchUtils.Options = {}) => {
  const token = localStorage.getItem("access_token");
  if (!token) {
    throw new Error("Không có token");
  }
  options.user = {
    authenticated: true,
    token: `Bearer ${token}`,
  };
  try {
    return await fetchUtils.fetchJson(url, options);
  } catch (error: any) {
    // Log the error for debugging
    if (error.status === 403) {
      console.warn("Access forbidden:", url);
    } else {
      console.error("HTTP Client Error:", {
        url,
        status: error.status,
        message: error.message,
      });
    }
    throw error;
  }
};

// base provider cho các resource bình thường (users, bookings, reports, ...)
const baseProvider = simpleRestProvider(apiUrl, httpClient) as DataProvider;

// dataProvider custom: override riêng getList cho "admin/payouts" và "bookings"
export const dataProvider: DataProvider = {
  ...baseProvider,

  async getList(resource, params) {
    // 👉 Resource bookings: handle custom response format
    if (resource === "bookings" || resource === "admin/bookings") {
      const { filter = {}, pagination, sort } = params;
      const { page = 1, perPage = 10 } = pagination || {};

      const query: Record<string, string> = {
        page: String(page),
        limit: String(perPage),
      };

      // Add filters
      Object.entries(filter).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") {
          query[key] = toQueryValue(value);
        }
      });

      const searchParams = new URLSearchParams(query);
      const url = `${apiUrl}/${resource}?${searchParams.toString()}`;

      const { json } = await httpClient(url);

      // Backend returns: { success, message, data: { bookings, total, page, totalPages } }
      const bookings = json?.data?.bookings ?? json?.bookings ?? [];
      const total = json?.data?.total ?? json?.total ?? 0;

      const data = bookings.map(normalizeId);

      return { data, total };
    }

    // ?? Resource admin/sessions: handle custom response format
    if (resource === "admin/sessions") {
      const { filter = {}, pagination } = params;
      const { page = 1, perPage = 25 } = pagination || {};

      const query: Record<string, string> = {
        page: String(page),
        limit: String(perPage),
      };

      Object.entries(filter).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") {
          query[key] = toQueryValue(value);
        }
      });

      const searchParams = new URLSearchParams(query);
      const url = `${apiUrl}/${resource}?${searchParams.toString()}`;

      const { json } = await httpClient(url);

      const sessions = json?.data?.sessions ?? json?.sessions ?? [];
      const total = json?.data?.total ?? json?.total ?? 0;
      const data = sessions.map(normalizeId);

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

  async getOne(resource, params) {
    if (resource === "admin/bookings") {
      const { json } = await httpClient(`${apiUrl}/${resource}/${params.id}`);
      const record = json?.data ?? json;
      return { data: normalizeId(record) };
    }

    return baseProvider.getOne(resource, params);
  },

  async update(resource, params) {
    if (resource === "admin/bookings") {
      const { json } = await httpClient(`${apiUrl}/${resource}/${params.id}`, {
        method: "PUT",
        body: JSON.stringify(params.data),
      });
      const record = json?.data ?? json;
      return { data: normalizeId(record) };
    }

    return baseProvider.update(resource, params);
  },
};
