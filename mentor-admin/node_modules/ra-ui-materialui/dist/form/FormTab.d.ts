import * as React from 'react';
import { ReactElement, ReactNode } from 'react';
import { StackProps, TabProps as MuiTabProps } from '@mui/material';
export declare const FormTab: {
    (props: FormTabProps): React.JSX.Element;
    displayName: string;
};
export interface FormTabProps extends Omit<StackProps, 'color'>, Omit<MuiTabProps, 'children' | 'classes' | 'ref'> {
    className?: string;
    children?: ReactNode;
    contentClassName?: string;
    count?: ReactNode;
    hidden?: boolean;
    icon?: ReactElement;
    intent?: 'header' | 'content';
    label: string | ReactElement;
    path?: string;
    resource?: string;
    syncWithLocation?: boolean;
    value?: string | number;
}
//# sourceMappingURL=FormTab.d.ts.map