# Socket trong MentorMe-Mobile-App

Tài liệu này mô tả cách sử dụng socket trong backend của ứng dụng MentorMe-Mobile-App.

## Công nghệ sử dụng

- **Socket.IO**: Thư viện hỗ trợ giao tiếp thời gian thực giữa client và server.

## Cài đặt

```bash
npm install socket.io
```

## Khởi tạo Socket Server

```js
const http = require("http");
const socketIo = require("socket.io");

const server = http.createServer(app);
const io = socketIo(server);

io.on("connection", (socket) => {
  console.log("User connected:", socket.id);

  socket.on("disconnect", () => {
    console.log("User disconnected:", socket.id);
  });

  // Lắng nghe sự kiện từ client
  socket.on("chat message", (msg) => {
    io.emit("chat message", msg);
  });
});

server.listen(3000, () => {
  console.log("Socket server running on port 3000");
});
```

## Các sự kiện phổ biến

- `connection`: Khi client kết nối.
- `disconnect`: Khi client ngắt kết nối.
- `chat message`: Nhận và gửi tin nhắn chat.

## Lưu ý

- Đảm bảo bảo mật khi truyền dữ liệu qua socket.
- Kiểm tra xác thực người dùng trước khi cho phép kết nối.

## Tài liệu tham khảo

- [Socket.IO Documentation](https://socket.io/docs/)
- [Node.js Documentation](https://nodejs.org/en/docs/)
