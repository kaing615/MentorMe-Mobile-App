"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useDataTableDataContext = exports.DataTableDataContext = void 0;
var react_1 = require("react");
exports.DataTableDataContext = (0, react_1.createContext)(undefined);
var useDataTableDataContext = function () {
    return (0, react_1.useContext)(exports.DataTableDataContext);
};
exports.useDataTableDataContext = useDataTableDataContext;
//# sourceMappingURL=DataTableDataContext.js.map