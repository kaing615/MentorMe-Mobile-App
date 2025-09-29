import * as React from 'react';
import { type ComponentsOverrides, type SxProps, type Theme } from '@mui/material/styles';
export declare const AccessDenied: (inProps: AccessDeniedProps) => React.JSX.Element;
export interface AccessDeniedProps {
    className?: string;
    textPrimary?: string;
    textSecondary?: string;
    icon?: React.ReactNode;
    sx?: SxProps<Theme>;
}
export declare const AccessDeniedClasses: {
    root: string;
    icon: string;
    message: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaAccessDenied: 'root' | 'icon' | 'message';
    }
    interface ComponentsPropsList {
        RaAccessDenied: Partial<AccessDeniedProps>;
    }
    interface Components {
        RaAccessDenied?: {
            defaultProps?: ComponentsPropsList['RaAccessDenied'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaAccessDenied'];
        };
    }
}
//# sourceMappingURL=AccessDenied.d.ts.map