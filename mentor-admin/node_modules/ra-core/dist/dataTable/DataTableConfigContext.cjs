"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useDataTableConfigContext = exports.DataTableConfigContext = void 0;
var react_1 = require("react");
exports.DataTableConfigContext = (0, react_1.createContext)({
    expandSingle: false,
    hover: true,
    hasBulkActions: false,
});
var useDataTableConfigContext = function () {
    return (0, react_1.useContext)(exports.DataTableConfigContext);
};
exports.useDataTableConfigContext = useDataTableConfigContext;
//# sourceMappingURL=DataTableConfigContext.js.map