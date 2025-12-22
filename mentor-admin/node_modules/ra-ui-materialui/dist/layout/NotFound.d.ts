import * as React from 'react';
import { type ComponentsOverrides, type Theme } from '@mui/material/styles';
import { type MUIStyledCommonProps } from '@mui/system';
export declare const NotFound: (inProps: NotFoundProps) => React.JSX.Element;
export interface NotFoundProps extends React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement>, MUIStyledCommonProps<Theme> {
}
export declare const NotFoundClasses: {
    icon: string;
    message: string;
    toolbar: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaNotFound: 'root';
    }
    interface ComponentsPropsList {
        RaNotFound: Partial<NotFoundProps>;
    }
    interface Components {
        RaNotFound?: {
            defaultProps?: ComponentsPropsList['RaNotFound'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaNotFound'];
        };
    }
}
//# sourceMappingURL=NotFound.d.ts.map