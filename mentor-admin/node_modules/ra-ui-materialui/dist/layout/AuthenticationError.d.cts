import * as React from 'react';
import { type ComponentsOverrides, type SxProps, type Theme } from '@mui/material/styles';
export declare const AuthenticationError: (inProps: AuthenticationErrorProps) => React.JSX.Element;
export interface AuthenticationErrorProps {
    className?: string;
    textPrimary?: string;
    textSecondary?: string;
    icon?: React.ReactNode;
    sx?: SxProps<Theme>;
}
export declare const AuthenticationErrorClasses: {
    root: string;
    icon: string;
    message: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaAuthenticationError: 'root' | 'icon' | 'message';
    }
    interface ComponentsPropsList {
        RaAuthenticationError: Partial<AuthenticationErrorProps>;
    }
    interface Components {
        RaAuthenticationError?: {
            defaultProps?: ComponentsPropsList['RaAuthenticationError'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaAuthenticationError'];
        };
    }
}
//# sourceMappingURL=AuthenticationError.d.ts.map