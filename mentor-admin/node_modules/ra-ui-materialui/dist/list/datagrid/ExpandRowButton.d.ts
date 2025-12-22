import * as React from 'react';
import { ElementType } from 'react';
import { IconButtonProps } from '@mui/material/IconButton';
export interface ExpandRowButtonProps extends IconButtonProps {
    component?: ElementType;
    expanded: boolean;
    expandContentId?: string;
}
declare const _default: React.MemoExoticComponent<({ expanded, expandContentId, ...props }: ExpandRowButtonProps) => React.JSX.Element>;
export default _default;
//# sourceMappingURL=ExpandRowButton.d.ts.map