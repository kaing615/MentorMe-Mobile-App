import { type ComponentsOverrides } from '@mui/material/styles';
import * as React from 'react';
import { type HtmlHTMLAttributes, type ReactNode } from 'react';
export declare const FilterForm: (inProps: FilterFormProps) => React.JSX.Element;
export type FilterFormProps = FilterFormBaseProps;
export declare const FilterFormBase: (props: FilterFormBaseProps) => React.JSX.Element;
export type FilterFormBaseProps = Omit<HtmlHTMLAttributes<HTMLFormElement>, 'children'> & {
    className?: string;
    resource?: string;
    filters?: ReactNode[];
};
export declare const FilterFormClasses: {
    clearFix: string;
    filterFormInput: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaFilterForm: 'root';
    }
    interface ComponentsPropsList {
        RaFilterForm: Partial<FilterFormProps>;
    }
    interface Components {
        RaFilterForm?: {
            defaultProps?: ComponentsPropsList['RaFilterForm'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaFilterForm'];
        };
    }
}
//# sourceMappingURL=FilterForm.d.ts.map