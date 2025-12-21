import { Box, Button, Card, CardContent, TextField, Typography } from "@mui/material";
import * as React from "react";
import { useNotify } from "react-admin";

export const MyProfile = () => {
  const notify = useNotify();
  const [currentPassword, setCurrentPassword] = React.useState("");
  const [newPassword, setNewPassword] = React.useState("");
  const [confirmPassword, setConfirmPassword] = React.useState("");
  const [loading, setLoading] = React.useState(false);

  const userEmail = localStorage.getItem("user_email") || "Admin";
  const userRole = localStorage.getItem("role") || "admin";

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!currentPassword || !newPassword || !confirmPassword) {
      notify("Please fill all fields", { type: "error" });
      return;
    }

    if (newPassword.length < 6) {
      notify("New password must be at least 6 characters", { type: "error" });
      return;
    }

    if (newPassword !== confirmPassword) {
      notify("Passwords do not match", { type: "error" });
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/users/change-password`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${localStorage.getItem("access_token")}`,
        },
        body: JSON.stringify({ currentPassword, newPassword }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Failed to change password");
      }

      notify("Password changed successfully", { type: "success" });
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (error: any) {
      notify(error.message || "Failed to change password", { type: "error" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ padding: 3 }}>
      <Typography variant="h4" gutterBottom>
        My Profile
      </Typography>

      <Card sx={{ maxWidth: 600, mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Account Information
          </Typography>
          <Typography variant="body1" gutterBottom>
            <strong>Email:</strong> {userEmail}
          </Typography>
          <Typography variant="body1">
            <strong>Role:</strong> {userRole}
          </Typography>
        </CardContent>
      </Card>

      <Card sx={{ maxWidth: 600 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Change Password
          </Typography>
          <Box component="form" onSubmit={handleChangePassword} sx={{ mt: 2 }}>
            <TextField
              fullWidth
              type="password"
              label="Current Password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              type="password"
              label="New Password (min 6 characters)"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              type="password"
              label="Confirm New Password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              margin="normal"
              required
            />
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={loading}
              sx={{ mt: 2 }}
            >
              {loading ? "Changing..." : "Change Password"}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};
