import * as React from 'react';
import { type ComponentsOverrides, type SxProps, type Theme } from '@mui/material/styles';
/**
 * The default DataTable Head component.
 *
 * Renders select all checkbox as well as column head buttons used for sorting.
 */
export declare const DataTableHead: React.MemoExoticComponent<(inProps: DataTableHeadProps) => React.JSX.Element>;
export interface DataTableHeadProps {
    children?: React.ReactNode;
    className?: string;
    size?: 'medium' | 'small';
    sx?: SxProps<Theme>;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaDataTableHead: 'root';
    }
    interface ComponentsPropsList {
        RaDataTableHead: Partial<DataTableHeadProps>;
    }
    interface Components {
        RaDataTableHead?: {
            defaultProps?: ComponentsPropsList['RaDataTableHead'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaDataTableHead'];
        };
    }
}
//# sourceMappingURL=DataTableHead.d.ts.map