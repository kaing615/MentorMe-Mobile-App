import { type FC } from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type PaginationProps } from '@mui/material';
export declare const PaginationActions: FC<PaginationActionsProps>;
export interface PaginationActionsProps extends PaginationProps {
    page: number;
    rowsPerPage: number;
    count: number;
    onPageChange: (event: MouseEvent, page: number) => void;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaPaginationActions: 'root';
    }
    interface ComponentsPropsList {
        RaPaginationActions: Partial<PaginationActionsProps>;
    }
    interface Components {
        RaPaginationActions?: {
            defaultProps?: ComponentsPropsList['RaPaginationActions'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaPaginationActions'];
        };
    }
}
//# sourceMappingURL=PaginationActions.d.ts.map