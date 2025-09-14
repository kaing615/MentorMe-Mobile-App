# axios/

Thư mục này chứa các hàm và file cấu hình để gọi API ra ngoài (tới bên thứ ba như VNPAY, Google, Mailgun, ...).

- Sử dụng TypeScript, file chính là `axios.client.ts`.
- Cấu hình baseURL, interceptors, headers mặc định nếu cần.
- Giúp tái sử dụng khi cần gọi nhiều API ngoài ở nhiều controller khác nhau.

**Ví dụ sử dụng:**

```ts
import axiosClient from "./axios.client";

const data = await axiosClient.get("https://api.example.com/resource");
```
