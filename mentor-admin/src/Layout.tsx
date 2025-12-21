import SettingsIcon from "@mui/icons-material/Settings";
import type { ReactNode } from 'react';
import {
  AppBar,
  CheckForApplicationUpdate,
  Logout,
  MenuItemLink,
  Layout as RALayout,
  UserMenu
} from "react-admin";

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

const MyAppBar = () => <AppBar userMenu={<MyUserMenu />} />;

export const Layout = ({ children }: { children: ReactNode }) => (
  <RALayout appBar={MyAppBar}>
    {children}
    <CheckForApplicationUpdate />
  </RALayout>
);
