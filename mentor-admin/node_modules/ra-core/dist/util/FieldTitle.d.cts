import * as React from 'react';
import { ReactNode } from 'react';
export interface FieldTitleProps {
    isRequired?: boolean;
    resource?: string;
    source?: string;
    label?: ReactNode;
}
export declare const FieldTitle: {
    (props: FieldTitleProps): number | Iterable<React.ReactNode> | React.JSX.Element | null;
    displayName: string;
};
declare const _default: React.MemoExoticComponent<{
    (props: FieldTitleProps): number | Iterable<React.ReactNode> | React.JSX.Element | null;
    displayName: string;
}>;
export default _default;
//# sourceMappingURL=FieldTitle.d.ts.map