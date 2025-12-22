import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import type { ReactNode } from 'react';
/**
 * Overrides Material UI CardContent to allow inner content
 *
 * When using several CardContent inside the same Card, the top and bottom
 * padding double the spacing between each CardContent, leading to too much
 * wasted space. Use this component as a CardContent alternative.
 */
export declare const CardContentInner: (inProps: CardContentInnerProps) => React.JSX.Element;
export interface CardContentInnerProps {
    className?: string;
    children: ReactNode;
}
export declare const CardContentInnerClasses: {
    root: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaCardContentInner: 'root';
    }
    interface ComponentsPropsList {
        RaCardContentInner: Partial<CardContentInnerProps>;
    }
    interface Components {
        RaCardContentInner?: {
            defaultProps?: ComponentsPropsList['RaCardContentInner'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaCardContentInner'];
        };
    }
}
//# sourceMappingURL=CardContentInner.d.ts.map