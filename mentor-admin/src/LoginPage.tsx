import { Box, Typography } from "@mui/material";
import { Login, LoginClasses, LoginForm } from "react-admin";

export const LoginPage = () => (
  <Login
    sx={{
      backgroundColor: "#f0f2f5",
      [`& .${LoginClasses.card}`]: {
        minWidth: 350,
        boxShadow: "0 2px 4px rgba(0,0,0,0.1), 0 8px 16px rgba(0,0,0,0.1)",
        borderRadius: "8px",
        padding: "20px",
      },
      [`& .${LoginClasses.avatar}`]: {
        bgcolor: "transparent",
      },
    }}
  >
     <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3, width: '100%' }}>
        <Typography variant="h5" color="primary" fontWeight="bold" gutterBottom>
            MentorMe Admin
        </Typography>
        <Typography variant="body2" color="text.secondary" align="center">
            Welcome back! Please login to continue.
        </Typography>
     </Box>
     <LoginForm />
  </Login>
);
