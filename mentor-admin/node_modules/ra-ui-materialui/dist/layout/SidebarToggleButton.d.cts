import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
/**
 * A button that toggles the sidebar. Used by default in the <AppBar>.
 * @param props The component props
 * @param {String} props.className An optional class name to apply to the button
 */
export declare const SidebarToggleButton: (inProps: SidebarToggleButtonProps) => React.JSX.Element;
export type SidebarToggleButtonProps = {
    className?: string;
};
export declare const SidebarToggleButtonClasses: {
    menuButtonIconClosed: string;
    menuButtonIconOpen: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaSidebarToggleButton: 'root' | 'menuButtonIconClosed' | 'menuButtonIconOpen';
    }
    interface ComponentsPropsList {
        RaSidebarToggleButton: Partial<SidebarToggleButtonProps>;
    }
    interface Components {
        RaSidebarToggleButton?: {
            defaultProps?: ComponentsPropsList['RaSidebarToggleButton'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaSidebarToggleButton'];
        };
    }
}
//# sourceMappingURL=SidebarToggleButton.d.ts.map