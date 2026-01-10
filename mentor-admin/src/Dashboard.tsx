import BookOnlineIcon from "@mui/icons-material/BookOnline";
import PeopleIcon from "@mui/icons-material/People";
import ReportIcon from "@mui/icons-material/Report";
import WorkIcon from "@mui/icons-material/Work";
import { Box, Card, CardContent, Grid, Typography } from "@mui/material";
import React from "react";
import { useGetList } from "react-admin";

const StatCard = ({
  title,
  value,
  icon,
  color,
}: {
  title: string;
  value?: number;
  icon: React.ReactNode;
  color: string;
}) => (
  <Card sx={{ height: "100%", display: "flex", alignItems: "center", p: 2 }}>
    <Box
      sx={{
        backgroundColor: color,
        borderRadius: "50%",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        width: 56,
        height: 56,
        mr: 2,
        color: "white",
      }}
    >
      {icon}
    </Box>
    <Box>
      <Typography color="text.secondary" variant="h6">
        {title}
      </Typography>
      <Typography variant="h4" component="div">
        {value === undefined ? "..." : value}
      </Typography>
    </Box>
  </Card>
);

export const Dashboard = () => {
  const { total: totalUsers } = useGetList(
    "users",
    {
      pagination: { page: 1, perPage: 1 },
    },
    { retry: false }
  );

  const { total: totalBookings } = useGetList(
    "admin/bookings",
    {
      pagination: { page: 1, perPage: 1 },
    },
    { retry: false }
  );

  const { total: totalApplications } = useGetList(
    "mentor-applications",
    {
      pagination: { page: 1, perPage: 1 },
    },
    { retry: false }
  );

  const { total: totalReports } = useGetList(
    "reports",
    {
      pagination: { page: 1, perPage: 1 },
    },
    { retry: false }
  );

  return (
    <Box mt={2}>
      <Typography variant="h4" gutterBottom>
        Bảng điều khiển MentorMe
      </Typography>
      <Typography variant="subtitle1" gutterBottom color="text.secondary" mb={4}>
        Tổng quan nhanh các chỉ số quan trọng.
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Tổng người dùng"
            value={totalUsers}
            icon={<PeopleIcon fontSize="large" />}
            color="#4f46e5"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Tổng lịch hẹn"
            value={totalBookings}
            icon={<BookOnlineIcon fontSize="large" />}
            color="#10b981"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Đơn đăng ký mentor"
            value={totalApplications}
            icon={<WorkIcon fontSize="large" />}
            color="#f59e0b"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Báo cáo"
            value={totalReports}
            icon={<ReportIcon fontSize="large" />}
            color="#ef4444"
          />
        </Grid>
      </Grid>

      <Box mt={4}>
        <Card>
          <CardContent>
            <Typography variant="h6">Gợi ý cho admin</Typography>
            <Typography variant="body2" color="text.secondary">
              - Kiểm tra "Đơn đăng ký mentor" hằng ngày.
              <br />
              - Xử lý "Báo cáo" để đảm bảo an toàn cộng đồng.
              <br />- Duyệt "Yêu cầu rút tiền" sớm trong kỳ.
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
};
