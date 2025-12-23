import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
export declare const Empty: (inProps: EmptyProps) => React.JSX.Element;
export interface EmptyProps {
    resource?: string;
    hasCreate?: boolean;
    className?: string;
}
export declare const EmptyClasses: {
    message: string;
    icon: string;
    toolbar: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaEmpty: 'root' | 'message' | 'icon' | 'toolbar';
    }
    interface ComponentsPropsList {
        RaEmpty: Partial<EmptyProps>;
    }
    interface Components {
        RaEmpty?: {
            defaultProps?: ComponentsPropsList['RaEmpty'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaEmpty'];
        };
    }
}
//# sourceMappingURL=Empty.d.ts.map