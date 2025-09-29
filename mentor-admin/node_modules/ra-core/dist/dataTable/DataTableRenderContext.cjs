"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.useDataTableRenderContext = exports.DataTableRenderContext = void 0;
var react_1 = require("react");
exports.DataTableRenderContext = (0, react_1.createContext)('data');
var useDataTableRenderContext = function () {
    return (0, react_1.useContext)(exports.DataTableRenderContext);
};
exports.useDataTableRenderContext = useDataTableRenderContext;
//# sourceMappingURL=DataTableRenderContext.js.map