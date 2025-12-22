import * as React from 'react';
export declare const FilterButtonMenuItem: React.ForwardRefExoticComponent<FilterButtonMenuItemProps & React.RefAttributes<any>>;
export interface FilterButtonMenuItemProps {
    filter: React.ReactElement;
    displayed: boolean;
    onShow: (params: {
        source: string;
        defaultValue: any;
    }) => void;
    onHide: (params: {
        source: string;
    }) => void;
    resource?: string;
    autoFocus?: boolean;
}
//# sourceMappingURL=FilterButtonMenuItem.d.ts.map