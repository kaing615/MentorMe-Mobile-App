"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
exports.usePreferenceKey = exports.PreferenceKeyContextProvider = exports.PreferenceKeyContext = void 0;
var React = __importStar(require("react"));
var react_1 = require("react");
exports.PreferenceKeyContext = (0, react_1.createContext)('');
var PreferenceKeyContextProvider = function (_a) {
    var _b = _a.value, value = _b === void 0 ? '' : _b, children = _a.children;
    return (React.createElement(exports.PreferenceKeyContext.Provider, { value: value }, children));
};
exports.PreferenceKeyContextProvider = PreferenceKeyContextProvider;
var usePreferenceKey = function () {
    return (0, react_1.useContext)(exports.PreferenceKeyContext);
};
exports.usePreferenceKey = usePreferenceKey;
//# sourceMappingURL=PreferenceKeyContext.js.map