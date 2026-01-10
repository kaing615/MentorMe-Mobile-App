import type { I18nProvider } from "react-admin";

const messages = {
  ra: {
    action: {
      add_filter: "Thêm bộ lọc",
      add: "Thêm",
      back: "Quay lại",
      cancel: "Hủy",
      clear_input_value: "Xóa giá trị",
      create: "Tạo mới",
      delete: "Xóa",
      edit: "Chỉnh sửa",
      refresh: "Làm mới",
      remove_filter: "Bỏ bộ lọc",
      save: "Lưu",
      search: "Tìm kiếm",
      show: "Chi tiết",
      undo: "Hoàn tác",
    },
    auth: {
      user_menu: "Tài khoản",
      username: "Tên đăng nhập",
      password: "Mật khẩu",
      sign_in: "Đăng nhập",
      sign_out: "Đăng xuất",
      forbidden: "Không có quyền truy cập",
      invalid_credentials: "Thông tin đăng nhập không đúng",
    },
    boolean: {
      true: "Có",
      false: "Không",
      null: "—",
    },
    message: {
      invalid_form: "Dữ liệu không hợp lệ",
      unsaved_changes: "Bạn có thay đổi chưa lưu",
    },
    navigation: {
      no_results: "Không có kết quả",
      no_more_results: "Không còn kết quả",
      page_out_of_boundaries: "Trang không hợp lệ",
      page_range_info: "%{offsetBegin}-%{offsetEnd} / %{total}",
      next: "Trang sau",
      prev: "Trang trước",
    },
    notification: {
      updated: "Đã cập nhật",
      created: "Đã tạo",
      deleted: "Đã xóa",
      bad_item: "Dữ liệu không hợp lệ",
      item_doesnt_exist: "Dữ liệu không tồn tại",
    },
    input: {
      file: {
        upload_several: "Kéo thả tệp để tải lên",
        upload_single: "Kéo thả tệp để tải lên",
      },
      image: {
        upload_several: "Kéo thả ảnh để tải lên",
        upload_single: "Kéo thả ảnh để tải lên",
      },
    },
    page: {
      list: "Danh sách",
      edit: "Chỉnh sửa",
      show: "Chi tiết",
      create: "Tạo mới",
    },
    sort: {
      sort_by: "Sắp xếp theo %{field} %{order}",
    },
    validation: {
      required: "Bắt buộc",
    },
  },
};

function interpolate(message: string, options: Record<string, any>) {
  return message.replace(/%\{([^}]+)\}/g, (_, key) =>
    options[key] !== undefined ? String(options[key]) : `%{${key}}`
  );
}

function resolveMessage(key: string): unknown {
  return key.split(".").reduce<unknown>((acc, part) => {
    if (acc && typeof acc === "object" && part in (acc as Record<string, unknown>)) {
      return (acc as Record<string, unknown>)[part];
    }
    return undefined;
  }, messages);
}

export const i18nProvider: I18nProvider = {
  translate: (key: string, options: Record<string, any> = {}) => {
    const message = resolveMessage(key);

    if (typeof message === "string") {
      return interpolate(message, options);
    }

    if (message && typeof message === "object" && typeof options.smart_count === "number") {
      const count = options.smart_count;
      const messageObj = message as { one?: string; other?: string };
      const picked = count === 1 ? messageObj.one ?? messageObj.other : messageObj.other ?? messageObj.one;
      if (typeof picked === "string") {
        return interpolate(picked, options);
      }
    }

    if (options._ !== undefined) {
      return options._;
    }

    return key;
  },
  changeLocale: () => Promise.resolve(),
  getLocale: () => "vi",
};
