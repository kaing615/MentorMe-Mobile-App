import * as React from 'react';
import { type ComponentsOverrides, type SxProps, type Theme } from '@mui/material';
export declare const AuthError: (inProps: AuthErrorProps) => React.JSX.Element;
export interface AuthErrorProps {
    className?: string;
    title?: string;
    message?: string;
    sx?: SxProps<Theme>;
}
export declare const AuthErrorClasses: {
    root: string;
    message: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaAuthError: 'root' | 'message';
    }
    interface ComponentsPropsList {
        RaAuthError: Partial<AuthErrorProps>;
    }
    interface Components {
        RaAuthError?: {
            defaultProps?: ComponentsPropsList['RaAuthError'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaAuthError'];
        };
    }
}
//# sourceMappingURL=AuthError.d.ts.map