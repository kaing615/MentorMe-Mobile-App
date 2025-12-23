import * as React from 'react';
import { type ComponentsOverrides, type Theme } from '@mui/material';
import { type MUIStyledCommonProps } from '@mui/system';
export declare const AuthLayout: (inProps: AuthLayoutProps) => React.JSX.Element;
export interface AuthLayoutProps extends MUIStyledCommonProps<Theme>, React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement> {
}
export declare const AuthLayoutClasses: {
    root: string;
    card: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaAuthLayout: 'root' | 'card';
    }
    interface ComponentsPropsList {
        RaAuthLayout: Partial<AuthLayoutProps>;
    }
    interface Components {
        RaAuthLayout?: {
            defaultProps?: ComponentsPropsList['RaAuthLayout'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaAuthLayout'];
        };
    }
}
//# sourceMappingURL=AuthLayout.d.ts.map