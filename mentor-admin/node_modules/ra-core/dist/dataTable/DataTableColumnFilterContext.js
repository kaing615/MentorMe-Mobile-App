import { createContext, useContext } from 'react';
export var DataTableColumnFilterContext = createContext(undefined);
export var useDataTableColumnFilterContext = function () {
    return useContext(DataTableColumnFilterContext);
};
//# sourceMappingURL=DataTableColumnFilterContext.js.map