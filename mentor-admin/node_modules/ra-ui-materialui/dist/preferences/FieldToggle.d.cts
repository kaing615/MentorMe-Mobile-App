import * as React from 'react';
import { ComponentsOverrides } from '@mui/material/styles';
/**
 * UI to enable/disable a field
 */
export declare const FieldToggle: (props: FieldToggleProps) => React.JSX.Element;
export interface FieldToggleProps {
    selected: boolean;
    label: React.ReactNode;
    onToggle?: (event: React.ChangeEvent<HTMLInputElement>) => void;
    onMove?: (dragIndex: string | number, dropIndex: string | number | null) => void;
    source: string;
    index: number | string;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaFieldToggle: 'root';
    }
    interface ComponentsPropsList {
        RaFieldToggle: Partial<FieldToggleProps>;
    }
    interface Components {
        RaFieldToggle?: {
            defaultProps?: ComponentsPropsList['RaFieldToggle'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaFieldToggle'];
        };
    }
}
//# sourceMappingURL=FieldToggle.d.ts.map