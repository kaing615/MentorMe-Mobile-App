import { type SxProps } from '@mui/material';
export type DatabaseRowSxContextType = ((record: any, index: number) => SxProps) | undefined;
export declare const DataTableRowSxContext: import("react").Context<DatabaseRowSxContextType>;
export declare const useDataTableRowSxContext: () => DatabaseRowSxContextType;
//# sourceMappingURL=DataTableRowSxContext.d.ts.map