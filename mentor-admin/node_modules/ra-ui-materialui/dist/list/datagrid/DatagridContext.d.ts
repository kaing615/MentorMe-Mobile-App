import { RaRecord } from 'ra-core';
declare const DatagridContext: import("react").Context<DatagridContextValue>;
export type DatagridContextValue = {
    isRowExpandable?: (record: RaRecord) => boolean;
    expandSingle?: boolean;
};
export default DatagridContext;
//# sourceMappingURL=DatagridContext.d.ts.map