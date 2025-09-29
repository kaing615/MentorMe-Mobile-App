import * as React from 'react';
import { Identifier, RaRecord, SortPayload } from 'ra-core';
/**
 * The default Datagrid Header component.
 *
 * Renders select all checkbox as well as column header buttons used for sorting.
 */
export declare const DatagridHeader: {
    (props: DatagridHeaderProps): React.JSX.Element;
    displayName: string;
};
export interface DatagridHeaderProps<RecordType extends RaRecord = any> {
    children?: React.ReactNode;
    className?: string;
    hasExpand?: boolean;
    hasBulkActions?: boolean;
    isRowSelectable?: (record: RecordType) => boolean;
    isRowExpandable?: (record: RecordType) => boolean;
    size?: 'medium' | 'small';
    sort?: SortPayload;
    data?: RecordType[];
    onSelect?: (ids: Identifier[]) => void;
    onToggleItem?: (id: Identifier) => void;
    selectedIds?: Identifier[];
    setSort?: (sort: SortPayload) => void;
}
//# sourceMappingURL=DatagridHeader.d.ts.map