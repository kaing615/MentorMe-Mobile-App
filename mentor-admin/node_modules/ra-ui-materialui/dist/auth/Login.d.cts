import * as React from 'react';
import { type HtmlHTMLAttributes, type ReactNode } from 'react';
import { type SxProps } from '@mui/material';
import { type ComponentsOverrides, type Theme } from '@mui/material/styles';
/**
 * A standalone login page, to serve as authentication gate to the admin
 *
 * Expects the user to enter a login and a password, which will be checked
 * by the `authProvider.login()` method. Redirects to the root page (/)
 * upon success, otherwise displays an authentication error message.
 *
 * Copy and adapt this component to implement your own login logic
 * (e.g. to authenticate via email or facebook or anything else).
 *
 * @example
 *     import MyLoginPage from './MyLoginPage';
 *     const App = () => (
 *         <Admin loginPage={MyLoginPage} authProvider={authProvider}>
 *             ...
 *        </Admin>
 *     );
 */
export declare const Login: (inProps: LoginProps) => React.JSX.Element;
export interface LoginProps extends HtmlHTMLAttributes<HTMLDivElement> {
    avatarIcon?: ReactNode;
    backgroundImage?: string;
    children?: ReactNode;
    className?: string;
    sx?: SxProps<Theme>;
}
export declare const LoginClasses: {
    card: string;
    avatar: string;
    icon: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaLogin: 'root' | 'card' | 'avatar' | 'icon';
    }
    interface ComponentsPropsList {
        RaLogin: Partial<LoginProps>;
    }
    interface Components {
        RaLogin?: {
            defaultProps?: ComponentsPropsList['RaLogin'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaLogin'];
        };
    }
}
//# sourceMappingURL=Login.d.ts.map