import React from 'react';
import { ComponentsOverrides } from '@mui/material/styles';
/**
 * Button toggling the theme (light or dark).
 *
 * Enabled by default in the <AppBar> when the <Admin> component has a darkMode.
 *
 * @example
 * import { AppBar, ToggleThemeButton } from 'react-admin';
 *
 * const MyAppBar = () => (
 *     <AppBar toolbar={<ToggleThemeButton />} />
 * );
 *
 * const MyLayout = ({ children }) => (
 *     <Layout appBar={MyAppBar}>
 *         {children}
 *     </Layout>
 * );
 */
export declare const ToggleThemeButton: () => React.JSX.Element;
declare const PREFIX = "RaToggleThemeButton";
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        [PREFIX]: 'root';
    }
    interface Components {
        [PREFIX]?: {
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>[typeof PREFIX];
        };
    }
}
export {};
//# sourceMappingURL=ToggleThemeButton.d.ts.map