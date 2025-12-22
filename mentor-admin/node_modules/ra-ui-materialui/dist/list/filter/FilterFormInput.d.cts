import * as React from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
export declare const FilterFormInput: (inProps: FilterFormInputProps) => React.JSX.Element;
export interface FilterFormInputProps {
    filterElement: React.ReactElement;
    handleHide: (event: React.MouseEvent<HTMLElement>) => void;
    className?: string;
    resource?: string;
}
export declare const FilterFormInputClasses: {
    spacer: string;
    hideButton: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaFilterFormInput: 'root' | 'spacer' | 'hideButton';
    }
    interface ComponentsPropsList {
        RaFilterFormInput: Partial<FilterFormInputProps>;
    }
    interface Components {
        RaFilterFormInput?: {
            defaultProps?: ComponentsPropsList['RaFilterFormInput'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaFilterFormInput'];
        };
    }
}
//# sourceMappingURL=FilterFormInput.d.ts.map