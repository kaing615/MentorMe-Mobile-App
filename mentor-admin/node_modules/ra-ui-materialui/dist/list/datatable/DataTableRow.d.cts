import React from 'react';
import { type TableRowProps } from '@mui/material';
import { type ComponentsOverrides } from '@mui/material/styles';
export interface DataTableRowProps extends Omit<TableRowProps, 'classes'> {
}
export declare const DataTableRow: React.MemoExoticComponent<React.ForwardRefExoticComponent<Omit<DataTableRowProps, "ref"> & React.RefAttributes<HTMLTableRowElement>>>;
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaDataTableRow: 'root';
    }
    interface ComponentsPropsList {
        RaDataTableRow: Partial<DataTableRowProps>;
    }
    interface Components {
        RaDataTableRow?: {
            defaultProps?: ComponentsPropsList['RaDataTableRow'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaDataTableRow'];
        };
    }
}
//# sourceMappingURL=DataTableRow.d.ts.map