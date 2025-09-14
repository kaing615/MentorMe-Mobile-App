# Validations

Tài liệu này mô tả các quy tắc kiểm tra hợp lệ được sử dụng trong backend của ứng dụng MentorMe Mobile.

## Đăng ký người dùng

- **Email**: Phải đúng định dạng email, bắt buộc, duy nhất.
- **Mật khẩu**: Tối thiểu 8 ký tự, ít nhất một chữ cái viết hoa, một chữ cái viết thường, một số.
- **Họ và tên**: Bắt buộc, tối đa 50 ký tự.

## Đăng nhập

- **Email**: Bắt buộc, phải đúng định dạng email.
- **Mật khẩu**: Bắt buộc.

## Cập nhật hồ sơ

- **Họ và tên**: Không bắt buộc, tối đa 50 ký tự.
- **Giới thiệu**: Không bắt buộc, tối đa 200 ký tự.
- **URL ảnh đại diện**: Không bắt buộc, phải là URL hợp lệ.

## Phiên làm việc

- **Token**: Bắt buộc, phải là JWT hợp lệ.

## Các mẫu kiểm tra hợp lệ chung

- Tất cả các trường bắt buộc phải có.
- Tất cả các trường kiểu chuỗi đều được loại bỏ khoảng trắng ở đầu và cuối.
- Tất cả các ID phải là UUID hợp lệ.

---

_Để biết chi tiết về logic kiểm tra hợp lệ, tham khảo các file service hoặc controller tương ứng._
