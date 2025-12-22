import { createContext, useContext } from 'react';
export var DataTableStoreContext = createContext({
    storeKey: '',
    defaultHiddenColumns: [],
});
export var useDataTableStoreContext = function () { return useContext(DataTableStoreContext); };
//# sourceMappingURL=DataTableStoreContext.js.map