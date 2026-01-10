import SettingsIcon from "@mui/icons-material/Settings";
import type { ReactNode } from "react";
import {
  AppBar,
  CheckForApplicationUpdate,
  Layout as RALayout,
  Logout,
  MenuItemLink,
  UserMenu,
} from "react-admin";

/* ===================== User Menu ===================== */

const MyUserMenu = () => (
  <UserMenu>
    <MenuItemLink
      to="/my-profile"
      primaryText="My Profile"
      leftIcon={<SettingsIcon />}
    />
    <Logout />
  </UserMenu>
);

/* ===================== App Bar ===================== */

const MyAppBar = () => <AppBar userMenu={<MyUserMenu />} />;

/* ===================== Layout ===================== */

export const Layout = ({ children }: { children: ReactNode }) => (
  <RALayout appBar={MyAppBar}>
    {children}
    <CheckForApplicationUpdate />
  </RALayout>
);
