import * as React from 'react';
import { type ToolbarProps } from '@mui/material';
import { type ComponentsOverrides } from '@mui/material/styles';
export declare const TopToolbar: (inProps: ToolbarProps) => React.JSX.Element;
export default TopToolbar;
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaTopToolbar: 'root';
    }
    interface ComponentsPropsList {
        RaTopToolbar: Partial<ToolbarProps>;
    }
    interface Components {
        RaTopToolbar?: {
            defaultProps?: ComponentsPropsList['RaTopToolbar'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaTopToolbar'];
        };
    }
}
//# sourceMappingURL=TopToolbar.d.ts.map