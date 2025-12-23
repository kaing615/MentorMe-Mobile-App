import { createContext, useContext } from 'react';
export var DataTableSelectedIdsContext = createContext(undefined);
export var useDataTableSelectedIdsContext = function () {
    return useContext(DataTableSelectedIdsContext);
};
//# sourceMappingURL=DataTableSelectedIdsContext.js.map