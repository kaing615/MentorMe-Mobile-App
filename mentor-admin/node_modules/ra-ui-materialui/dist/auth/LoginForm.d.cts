import * as React from 'react';
import { type ComponentsOverrides, type SxProps, type Theme } from '@mui/material/styles';
export declare const LoginForm: (inProps: LoginFormProps) => React.JSX.Element;
export declare const LoginFormClasses: {
    content: string;
    button: string;
    icon: string;
};
export interface LoginFormProps {
    redirectTo?: string;
    className?: string;
    sx?: SxProps<Theme>;
    children?: React.ReactNode;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaLoginForm: 'root' | 'content' | 'button' | 'icon';
    }
    interface ComponentsPropsList {
        RaLoginForm: Partial<LoginFormProps>;
    }
    interface Components {
        RaLoginForm?: {
            defaultProps?: ComponentsPropsList['RaLoginForm'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaLoginForm'];
        };
    }
}
//# sourceMappingURL=LoginForm.d.ts.map