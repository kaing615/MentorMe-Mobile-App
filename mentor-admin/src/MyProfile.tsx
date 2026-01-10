import * as React from "react";
import {
  Box,
  Button,
  Card,
  CardContent,
  TextField,
  Typography,
  Avatar,
  Chip,
  Divider,
  IconButton,
  InputAdornment,
  Stack,
} from "@mui/material";
import {
  Visibility,
  VisibilityOff,
  LockOutlined,
  PersonOutline,
} from "@mui/icons-material";
import { useNotify } from "react-admin";

export const MyProfile = () => {
  const notify = useNotify();

  const [currentPassword, setCurrentPassword] = React.useState("");
  const [newPassword, setNewPassword] = React.useState("");
  const [confirmPassword, setConfirmPassword] = React.useState("");
  const [loading, setLoading] = React.useState(false);

  const [showCurrent, setShowCurrent] = React.useState(false);
  const [showNew, setShowNew] = React.useState(false);
  const [showConfirm, setShowConfirm] = React.useState(false);

  const email = localStorage.getItem("user_email") || "admin@system.com";
  const role = localStorage.getItem("role") || "admin";
  const token = localStorage.getItem("access_token") || "";

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!currentPassword || !newPassword || !confirmPassword) {
      notify("Vui lòng nhập đầy đủ.", { type: "warning" });
      return;
    }

    if (newPassword.length < 6) {
      notify("Mật khẩu phải tối thiểu 6 ký tự.", { type: "error" });
      return;
    }

    if (newPassword !== confirmPassword) {
      notify("Mật khẩu không khớp.", { type: "error" });
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(
        `${import.meta.env.VITE_API_URL}/users/change-password`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            currentPassword,
            newPassword,
          }),
        }
      );

      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || "Đổi mật khẩu thất bại");
      }

      notify("Đổi mật khẩu thành công", { type: "success" });
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (e: any) {
      notify(e.message || "Đổi mật khẩu thất bại", { type: "error" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 900, mx: "auto", p: 3 }}>
      <Typography variant="h4" fontWeight={600} gutterBottom>
        Cài đặt tài khoản
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Quản lý thông tin và bảo mật tài khoản
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack direction="row" spacing={2} alignItems="center">
            <Avatar sx={{ width: 56, height: 56 }}>
              <PersonOutline />
            </Avatar>

            <Box flex={1}>
              <Typography fontWeight={600}>{email}</Typography>
              <Stack direction="row" spacing={1} mt={0.5}>
                <Chip size="small" color="primary" label={role.toUpperCase()} />
              </Stack>
            </Box>
          </Stack>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Stack direction="row" spacing={1} alignItems="center" mb={1}>
            <LockOutlined fontSize="small" />
            <Typography variant="h6">Bảo mật</Typography>
          </Stack>

          <Typography color="text.secondary" fontSize={14} mb={2}>
            Cập nhật mật khẩu để bảo vệ tài khoản
          </Typography>

          <Divider sx={{ mb: 2 }} />

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Mật khẩu hiện tại"
              type={showCurrent ? "text" : "password"}
              margin="normal"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowCurrent(!showCurrent)}
                      edge="end"
                    >
                      {showCurrent ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              fullWidth
              label="Mật khẩu mới"
              type={showNew ? "text" : "password"}
              margin="normal"
              helperText="Tối thiểu 6 ký tự"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowNew(!showNew)} edge="end">
                      {showNew ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              fullWidth
              label="Xác nhận mật khẩu mới"
              type={showConfirm ? "text" : "password"}
              margin="normal"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowConfirm(!showConfirm)}
                      edge="end"
                    >
                      {showConfirm ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button
              type="submit"
              variant="contained"
              sx={{ mt: 3 }}
              disabled={loading}
            >
              {loading ? "Đang cập nhật..." : "Cập nhật mật khẩu"}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};
