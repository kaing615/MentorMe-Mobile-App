import BookOnlineIcon from "@mui/icons-material/BookOnline";
import PeopleIcon from "@mui/icons-material/People";
import ReportIcon from "@mui/icons-material/Report";
import WorkIcon from "@mui/icons-material/Work";
import { Box, Card, CardContent, Grid, Typography } from "@mui/material";
import React from "react";
import { useGetList, usePermissions } from "react-admin";

const StatCard = ({ title, value, icon, color }: { title: string, value?: number, icon: React.ReactNode, color: string }) => (
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
      <Typography color="textSecondary" variant="h6">
        {title}
      </Typography>
      <Typography variant="h4" component="div">
        {value === undefined ? "..." : value}
      </Typography>
    </Box>
  </Card>
);

export const Dashboard = () => {
    const { permissions } = usePermissions();

    const { total: totalUsers } = useGetList("users", {
        pagination: { page: 1, perPage: 1 },
    }, { retry: false });

    const { total: totalBookings } = useGetList("admin/bookings", {
        pagination: { page: 1, perPage: 1 },
    }, { retry: false });

    const { total: totalApplications } = useGetList("mentor-applications", {
        pagination: { page: 1, perPage: 1 },
    }, { enabled: false }); // Disable this API call as it returns 404
    
    const { total: totalReports } = useGetList("reports", {
        pagination: { page: 1, perPage: 1 },
    }, { retry: false });


  return (
    <Box mt={2}>
      <Typography variant="h4" gutterBottom>
        Welcome to MentorMe Admin
      </Typography>
      <Typography variant="subtitle1" gutterBottom color="textSecondary" mb={4}>
        Overview of key metrics and activities.
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Users"
            value={totalUsers}
            icon={<PeopleIcon fontSize="large" />}
            color="#4f46e5" // Indigo
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Bookings"
            value={totalBookings}
            icon={<BookOnlineIcon fontSize="large" />}
            color="#10b981" // Emerald
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Mentor Applications"
            value={totalApplications}
            icon={<WorkIcon fontSize="large" />}
            color="#f59e0b" // Amber
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Reports"
            value={totalReports}
            icon={<ReportIcon fontSize="large" />}
            color="#ef4444" // Red
          />
        </Grid>
      </Grid>
      
      {/* Additional sections could be added here, like Recent Activity */}
      <Box mt={4}>
        <Card>
            <CardContent>
                <Typography variant="h6">Admin Tips</Typography>
                <Typography variant="body2" color="textSecondary">
                    - Check "Mentor Applications" daily for new requests.<br/>
                    - Review "Reports" to ensure community safety.<br/>
                    - Manage Payouts early in the billing cycle.
                </Typography>
            </CardContent>
        </Card>
      </Box>
    </Box>
  );
};
