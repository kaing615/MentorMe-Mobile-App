import * as React from 'react';
import { type SortPayload, type ExtractRecordPaths, type HintedString } from 'ra-core';
import { type SxProps, type TableCellProps } from '@mui/material';
type NoInfer<T> = T extends infer U ? U : never;
export interface DataTableColumnProps<RecordType extends Record<string, any> = Record<string, any>> extends Omit<TableCellProps, 'component'> {
    cellSx?: (record: RecordType) => SxProps;
    cellClassName?: string;
    headerClassName?: string;
    render?: (record: RecordType) => React.ReactNode;
    field?: React.ElementType;
    source?: NoInfer<HintedString<ExtractRecordPaths<RecordType>>>;
    label?: React.ReactNode;
    disableSort?: boolean;
    sortByOrder?: SortPayload['order'];
}
export declare const DataTableColumn: <RecordType extends Record<string, any> = Record<string, any>>(props: DataTableColumnProps<RecordType>) => React.JSX.Element;
export {};
//# sourceMappingURL=DataTableColumn.d.ts.map