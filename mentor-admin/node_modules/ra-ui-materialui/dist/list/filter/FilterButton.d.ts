import * as React from 'react';
import { type ReactNode, type HtmlHTMLAttributes } from 'react';
import { type ButtonProps as MuiButtonProps, type ComponentsOverrides } from '@mui/material';
export declare const FilterButton: (inProps: FilterButtonProps) => React.JSX.Element | null;
export interface FilterButtonProps extends HtmlHTMLAttributes<HTMLDivElement>, Pick<MuiButtonProps, 'variant' | 'size'> {
    className?: string;
    disableSaveQuery?: boolean;
    filters?: ReactNode[];
    resource?: string;
}
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaFilterButton: 'root';
    }
    interface ComponentsPropsList {
        RaFilterButton: Partial<FilterButtonProps>;
    }
    interface Components {
        RaFilterButton?: {
            defaultProps?: ComponentsPropsList['RaFilterButton'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaFilterButton'];
        };
    }
}
//# sourceMappingURL=FilterButton.d.ts.map