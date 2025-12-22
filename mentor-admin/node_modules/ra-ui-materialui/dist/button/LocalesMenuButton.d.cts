import * as React from 'react';
import { type ReactNode } from 'react';
import { type ComponentsOverrides } from '@mui/material';
/**
 * Language selector. Changes the locale in the app and persists it in
 * preferences so that the app opens with the right locale in the future.
 *
 * Uses i18nProvider.getLocales() to get the list of available locales.
 *
 * @example
 * import { AppBar, TitlePortal, LocalesMenuButton } from 'react-admin';
 *
 * const MyAppBar = () => (
 *     <AppBar>
 *         <TitlePortal />
 *         <LocalesMenuButton />
 *     </AppBar>
 * );
 */
export declare const LocalesMenuButton: (inProps: LocalesMenuButtonProps) => React.JSX.Element;
export declare const LocalesMenuButtonClasses: {};
export interface LocalesMenuButtonProps {
    icon?: ReactNode;
    languages?: {
        locale: string;
        name: string;
    }[];
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaLocalesMenuButton: 'root';
    }
    interface ComponentsPropsList {
        RaLocalesMenuButton: Partial<LocalesMenuButtonProps>;
    }
    interface Components {
        RaLocalesMenuButton?: {
            defaultProps?: ComponentsPropsList['RaLocalesMenuButton'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaLocalesMenuButton'];
        };
    }
}
//# sourceMappingURL=LocalesMenuButton.d.ts.map