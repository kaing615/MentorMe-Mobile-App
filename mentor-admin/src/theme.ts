import { createTheme } from "@mui/material/styles";
import { defaultTheme } from "react-admin";

export const lightTheme = createTheme({
  ...defaultTheme,
  palette: {
    ...defaultTheme.palette,
    mode: "light",
    primary: {
      main: "#4f46e5", // Indigo
    },
    secondary: {
      main: "#ec4899", // Pink
    },
    background: {
      default: "#f9fafb", // Gray 50
      paper: "#ffffff",
    },
  },
  typography: {
    fontFamily: [
        'Inter',
        '-apple-system',
        'BlinkMacSystemFont',
        '"Segoe UI"',
        'Roboto',
        '"Helvetica Neue"',
        'Arial',
        'sans-serif',
      ].join(','),
  },
  components: {
    ...defaultTheme.components,
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: "#ffffff",
          color: "#1f2937", // Gray 800
          borderBottom: "1px solid #e5e7eb",
          boxShadow: "none",
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: "none",
          fontWeight: 600,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: "0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)",
        },
      },
    },
  },
});

export const darkTheme = createTheme({
  ...defaultTheme,
  palette: {
    ...defaultTheme.palette,
    mode: "dark",
    primary: {
        main: "#818cf8", // Indigo 400
    },
    secondary: {
        main: "#f472b6", // Pink 400
    },
    background: {
        default: "#111827", // Gray 900
        paper: "#1f2937", // Gray 800
    },
  },
  typography: {
    fontFamily: [
        'Inter',
        '-apple-system',
        'BlinkMacSystemFont',
        '"Segoe UI"',
        'Roboto',
        '"Helvetica Neue"',
        'Arial',
        'sans-serif',
      ].join(','),
  },
    components: {
        ...defaultTheme.components,
        MuiAppBar: {
            styleOverrides: {
                root: {
                    backgroundColor: "#1f2937", // Gray 800
                    color: "#ffffff",
                    borderBottom: "1px solid #374151",
                    boxShadow: "none",
                },
            },
        },
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 8,
                    textTransform: "none",
                    fontWeight: 600,
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                    boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)",
                    backgroundColor: "#1f2937",
                },
            },
        },
    }
});
