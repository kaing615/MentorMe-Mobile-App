import { type ComponentsOverrides } from '@mui/material/styles';
import * as React from 'react';
import { type ReactNode } from 'react';
/**
 * The UserMenu component renders a Mui Button that shows a Menu.
 * It accepts children that must be Mui MenuItem components.
 *
 * @example
 * import { Logout, UserMenu, useUserMenu } from 'react-admin';
 * import MenuItem from '@mui/material/MenuItem';
 * import ListItemIcon from '@mui/material/ListItemIcon';
 * import ListItemText from '@mui/material/ListItemText';
 * import SettingsIcon from '@mui/icons-material/Settings';

 * const ConfigurationMenu = React.forwardRef((props, ref) => {
 *     const { onClose } = useUserMenu();
 *     return (
 *         <MenuItem
 *             ref={ref}
 *             {...props}
 *             to="/configuration"
 *             onClick={onClose}
 *         >
 *             <ListItemIcon>
 *                 <SettingsIcon />
 *             </ListItemIcon>
 *             <ListItemText>Configuration</ListItemText>
 *         </MenuItem>
 *     );
 * });
 *
 * export const MyUserMenu = () => (
 *     <UserMenu>
 *         <ConfigurationMenu />
 *         <Logout />
 *     </UserMenu>
 * );
 * @param props
 * @param {ReactNode} props.children React node/s to be rendered as children of the UserMenu. Must be Mui MenuItem components
 * @param {string} props.className CSS class applied to the MuiAppBar component
 * @param {string} props.label The label of the UserMenu button. Accepts translation keys
 * @param {Element} props.icon The icon of the UserMenu button.
 *
 */
export declare const UserMenu: (inProps: UserMenuProps) => React.JSX.Element | null;
export interface UserMenuProps {
    children?: ReactNode;
    className?: string;
    label?: string;
    icon?: ReactNode;
}
export declare const UserMenuClasses: {
    userButton: string;
    avatar: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaUserMenu: 'root' | 'userButton' | 'avatar';
    }
    interface ComponentsPropsList {
        RaUserMenu: Partial<UserMenuProps>;
    }
    interface Components {
        RaUserMenu?: {
            defaultProps?: ComponentsPropsList['RaUserMenu'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaUserMenu'];
        };
    }
}
//# sourceMappingURL=UserMenu.d.ts.map