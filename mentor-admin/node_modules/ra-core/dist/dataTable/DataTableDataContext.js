import { createContext, useContext } from 'react';
export var DataTableDataContext = createContext(undefined);
export var useDataTableDataContext = function () {
    return useContext(DataTableDataContext);
};
//# sourceMappingURL=DataTableDataContext.js.map