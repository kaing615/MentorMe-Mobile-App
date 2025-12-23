import * as React from 'react';
import { type ComponentsOverrides, type Theme } from '@mui/material/styles';
import { MUIStyledCommonProps } from '@mui/system';
interface PlaceholderProps extends MUIStyledCommonProps<Theme> {
    className?: string;
}
export declare const Placeholder: (inProps: PlaceholderProps) => React.JSX.Element;
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaPlaceholder: 'root';
    }
    interface ComponentsPropsList {
        RaPlaceholder: Partial<PlaceholderProps>;
    }
    interface Components {
        RaPlaceholder?: {
            defaultProps?: ComponentsPropsList['RaPlaceholder'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaPlaceholder'];
        };
    }
}
export {};
//# sourceMappingURL=Placeholder.d.ts.map