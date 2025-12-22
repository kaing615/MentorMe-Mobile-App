import { ReactElement, ReactNode } from 'react';
import { TabProps as MuiTabProps } from '@mui/material';
export declare const FormTabHeader: ({ count, label, value, icon, className, onChange, syncWithLocation, ...rest }: FormTabHeaderProps) => ReactElement;
interface FormTabHeaderProps extends Omit<MuiTabProps, 'children'> {
    className?: string;
    count?: ReactNode;
    hidden?: boolean;
    icon?: ReactElement;
    intent?: 'header' | 'content';
    label: string | ReactElement;
    margin?: 'none' | 'normal' | 'dense';
    onChange?: (event: any) => void;
    path?: string;
    resource?: string;
    syncWithLocation?: boolean;
    value: string | number;
    variant?: 'standard' | 'outlined' | 'filled';
}
export {};
//# sourceMappingURL=FormTabHeader.d.ts.map