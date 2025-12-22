import * as React from 'react';
import { type ComponentsOverrides, type Theme } from '@mui/material/styles';
import { type SxProps } from '@mui/material';
export declare const Loading: (inProps: LoadingProps) => React.JSX.Element | null;
export interface LoadingProps {
    className?: string;
    loadingPrimary?: string;
    loadingSecondary?: string;
    timeout?: number;
    sx?: SxProps<Theme>;
}
export declare const LoadingClasses: {
    root: string;
    icon: string;
    message: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaLoading: 'root' | 'icon' | 'message';
    }
    interface ComponentsPropsList {
        RaLoading: Partial<LoadingProps>;
    }
    interface Components {
        RaLoading?: {
            defaultProps?: ComponentsPropsList['RaLoading'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaLoading'];
        };
    }
}
//# sourceMappingURL=Loading.d.ts.map