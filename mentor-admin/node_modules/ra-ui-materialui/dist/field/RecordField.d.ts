import * as React from 'react';
import { type ReactNode, type ElementType } from 'react';
import { type ComponentsOverrides } from '@mui/material/styles';
import { type StackProps, type SxProps, type TypographyProps } from '@mui/material';
import { type ExtractRecordPaths, type HintedString } from 'ra-core';
export declare const RecordField: <RecordType extends Record<string, any> = Record<string, any>>(inProps: RecordFieldProps<RecordType>) => React.JSX.Element | null;
type NoInfer<T> = T extends infer U ? U : never;
export interface RecordFieldProps<RecordType extends Record<string, any> = Record<string, any>> extends StackProps {
    children?: ReactNode;
    className?: string;
    empty?: ReactNode;
    field?: ElementType;
    label?: ReactNode;
    render?: (record: RecordType) => React.ReactNode;
    source?: NoInfer<HintedString<ExtractRecordPaths<RecordType>>>;
    record?: RecordType;
    sx?: SxProps;
    TypographyProps?: TypographyProps;
    variant?: 'default' | 'inline';
}
export declare const RecordFieldClasses: {
    label: string;
    value: string;
    inline: string;
};
declare module '@mui/material/styles' {
    interface ComponentNameToClassKey {
        RaRecordField: 'root' | 'label' | 'value' | 'inline';
    }
    interface ComponentsPropsList {
        RaRecordField: Partial<RecordFieldProps>;
    }
    interface Components {
        RaRecordField?: {
            defaultProps?: ComponentsPropsList['RaRecordField'];
            styleOverrides?: ComponentsOverrides<Omit<Theme, 'components'>>['RaRecordField'];
        };
    }
}
export {};
//# sourceMappingURL=RecordField.d.ts.map