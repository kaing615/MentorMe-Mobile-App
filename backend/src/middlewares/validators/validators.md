# Validators

Các validator trong thư mục này được sử dụng để kiểm tra và xác thực dữ liệu đầu vào từ client trước khi xử lý trong các route của ứng dụng. Việc sử dụng validator giúp đảm bảo dữ liệu hợp lệ, giảm thiểu lỗi và tăng tính bảo mật cho hệ thống.

## Cách sử dụng

1. Import validator cần thiết vào file route:

   ```ts
   import { exampleValidator } from "../middlewares/validators/exampleValidator";
   ```

2. Thêm validator vào middleware của route:

   ```ts
   router.post("/example", exampleValidator, controller.exampleHandler);
   ```

## Quy tắc đặt tên

- Tên file validator nên theo dạng `<entity>Validator.ts`.
- Hàm validator nên có tên rõ ràng, thể hiện chức năng kiểm tra.

## Ví dụ

```ts
// exampleValidator.ts
import { body } from "express-validator";
import { RequestHandler } from "express";

export const exampleValidator: RequestHandler[] = [
  body("email").isEmail().withMessage("Email không hợp lệ"),
  body("password")
    .isLength({ min: 6 })
    .withMessage("Mật khẩu phải có ít nhất 6 ký tự"),
];
```

## Lưu ý

- Luôn kiểm tra kết quả xác thực và trả về lỗi nếu dữ liệu không hợp lệ.
- Có thể kết hợp nhiều validator cho một route nếu cần thiết.
