import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type ReactNode } from 'react';
import { type MenuItemProps } from '@mui/material';
/**
 * Logout button component, to be passed to the Admin component
 *
 * Used for the Logout Menu item in the sidebar
 */
export declare const Logout: React.ForwardRefExoticComponent<Omit<MenuItemProps<'li'>, 'ref'> & React.RefAttributes<HTMLLIElement> & LogoutProps>;
export declare const LogoutClasses: {
    icon: string;
};
export interface LogoutProps {
    className?: string;
    redirectTo?: string;
    icon?: ReactNode;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaLogout: 'root' | 'icon';
    }
    interface ComponentsPropsList {
        RaLogout: Partial<LogoutProps>;
    }
    interface Components {
        RaLogout?: {
            defaultProps?: ComponentsPropsList['RaLogout'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaLogout'];
        };
    }
}
//# sourceMappingURL=Logout.d.ts.map