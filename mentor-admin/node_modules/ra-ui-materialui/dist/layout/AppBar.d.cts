import * as React from 'react';
import { FC } from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type AppBarProps as MuiAppBarProps } from '@mui/material';
/**
 * The AppBar component renders a custom MuiAppBar.
 *
 * @param {Object} props
 * @param {ReactNode} props.children React node/s to be rendered as children of the AppBar
 * @param {string} props.className CSS class applied to the MuiAppBar component
 * @param {string} props.color The color of the AppBar
 * @param {Element | boolean} props.userMenu A custom user menu component for the AppBar. <UserMenu/> component by default. Pass false to disable.
 *
 * @example // add a custom button to the AppBar
 *
 * const MyAppBar = () => (
 *   <AppBar>
 *     <TitlePortal />
 *     <MyCustomButton />
 *   </AppBar>
 * );
 *
 * @example // without a user menu
 *
 * const MyAppBar = () => <AppBar userMenu={false} />;
 */
export declare const AppBar: FC<AppBarProps>;
export interface AppBarProps extends MuiAppBarProps {
    /**
     * This prop is injected by Layout. You should not use it directly unless
     * you are using a custom layout.
     * If you are using the default layout, use `<Layout appBarAlwaysOn>` instead.
     */
    alwaysOn?: boolean;
    container?: React.ElementType<any>;
    toolbar?: React.ReactNode;
    userMenu?: React.ReactNode;
}
export declare const AppBarClasses: {
    appBar: string;
    toolbar: string;
    menuButton: string;
    menuButtonIconClosed: string;
    menuButtonIconOpen: string;
    title: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaAppBar: 'root' | 'appBar' | 'toolbar' | 'menuButton' | 'menuButtonIconClosed' | 'menuButtonIconOpen' | 'title';
    }
    interface ComponentsPropsList {
        RaAppBar: Partial<AppBarProps>;
    }
    interface Components {
        RaAppBar?: {
            defaultProps?: ComponentsPropsList['RaAppBar'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaAppBar'];
        };
    }
}
//# sourceMappingURL=AppBar.d.ts.map