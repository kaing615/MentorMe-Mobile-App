import * as React from 'react';
import { ReactNode, FC } from 'react';
import { Identifier, RaRecord } from 'ra-core';
export declare const DataTableLoading: React.NamedExoticComponent<DataTableLoadingProps<any>>;
export interface DataTableLoadingProps<RecordType extends RaRecord = any> {
    className?: string;
    expand?: ReactNode | FC<{
        id: Identifier;
        record: RecordType;
        resource: string;
    }>;
    hasBulkActions?: boolean;
    nbChildren: number;
    nbFakeLines?: number;
    size?: 'small' | 'medium';
}
//# sourceMappingURL=DataTableLoading.d.ts.map