import * as React from 'react';
import { ComponentsOverrides } from '@mui/material/styles';
export declare const Inspector: {
    (): React.JSX.Element | null;
    displayName: string;
};
export declare const InspectorClasses: {
    modal: string;
    title: string;
    content: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaInspector: 'root' | 'modal' | 'title' | 'content';
    }
    interface Components {
        RaInspector?: {
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaInspector'];
        };
    }
}
//# sourceMappingURL=Inspector.d.ts.map