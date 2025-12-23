import * as React from 'react';
import { type ComponentType, type ErrorInfo, type HtmlHTMLAttributes } from 'react';
import type { FallbackProps } from 'react-error-boundary';
import { type ComponentsOverrides, type Theme } from '@mui/material/styles';
import { type SxProps } from '@mui/material';
import type { TitleComponent } from 'ra-core';
export declare const Error: (inProps: InternalErrorProps & {
    errorComponent?: ComponentType<ErrorProps>;
} & {
    sx?: SxProps<Theme>;
}) => React.JSX.Element;
interface InternalErrorProps extends Omit<HtmlHTMLAttributes<HTMLDivElement>, 'title'>, FallbackProps {
    className?: string;
    errorInfo?: ErrorInfo;
}
export interface ErrorProps extends Pick<FallbackProps, 'error'> {
    errorInfo?: ErrorInfo;
    title?: TitleComponent;
}
export declare const ErrorClasses: {
    container: string;
    title: string;
    icon: string;
    panel: string;
    panelSummary: string;
    panelDetails: string;
    toolbar: string;
    advice: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaError: 'root' | 'title' | 'icon' | 'panel' | 'panelSumary' | 'panelDetails' | 'toolbar' | 'advice';
    }
    interface ComponentsPropsList {
        RaError: Partial<ErrorProps>;
    }
    interface Components {
        RaError?: {
            defaultProps?: ComponentsPropsList['RaError'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaError'];
        };
    }
}
export {};
//# sourceMappingURL=Error.d.ts.map