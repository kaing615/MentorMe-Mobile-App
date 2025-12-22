import * as React from 'react';
import { type ComponentType } from 'react';
import { type TableBodyProps } from '@mui/material';
import { type ComponentsOverrides } from '@mui/material/styles';
export declare const DataTableBody: React.MemoExoticComponent<React.ForwardRefExoticComponent<Omit<DataTableBodyProps, "ref"> & React.RefAttributes<HTMLTableSectionElement>>>;
export interface DataTableBodyProps extends TableBodyProps {
    row?: ComponentType;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        DataTableBody: 'root';
    }
    interface ComponentsPropsList {
        DataTableBody: Partial<DataTableBodyProps>;
    }
    interface Components {
        DataTableBody?: {
            defaultProps?: ComponentsPropsList['DataTableBody'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['DataTableBody'];
        };
    }
}
//# sourceMappingURL=DataTableBody.d.ts.map