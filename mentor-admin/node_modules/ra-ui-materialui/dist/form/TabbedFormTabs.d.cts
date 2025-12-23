import * as React from 'react';
import { ReactElement } from 'react';
import { TabsProps } from '@mui/material/Tabs';
export declare const TabbedFormTabs: (props: TabbedFormTabsProps) => React.JSX.Element;
export declare const getTabbedFormTabFullPath: (tab: ReactElement, index: number) => string;
export interface TabbedFormTabsProps extends TabsProps {
    url?: string;
    tabsWithErrors?: string[];
    syncWithLocation?: boolean;
}
//# sourceMappingURL=TabbedFormTabs.d.ts.map