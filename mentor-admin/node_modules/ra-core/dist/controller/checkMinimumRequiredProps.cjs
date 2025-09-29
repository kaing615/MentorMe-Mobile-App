"use strict";
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
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
exports.useCheckMinimumRequiredProps = void 0;
var React = __importStar(require("react"));
var checkMinimumRequiredProps = function (displayName, requiredProps) {
    return function (WrappedComponent) {
        return function (props) {
            (0, exports.useCheckMinimumRequiredProps)(displayName, requiredProps, props);
            return React.createElement(WrappedComponent, __assign({}, props));
        };
    };
};
exports.default = checkMinimumRequiredProps;
// Not a hook but named that way to avoid conflicts with the old one
var useCheckMinimumRequiredProps = function (displayName, requiredProps, props) {
    var propNames = Object.keys(props);
    var missingProps = requiredProps.filter(function (prop) { return !propNames.includes(prop); });
    if (missingProps.length > 0) {
        throw new Error("<".concat(displayName, "> component is not properly configured, some essential props are missing.\nBe sure to pass the props from the parent. Example:\n\nconst My").concat(displayName, " = props => (\n    <").concat(displayName, " {...props}></").concat(displayName, ">\n);\n\nThe missing props are: ").concat(missingProps.join(', ')));
    }
};
exports.useCheckMinimumRequiredProps = useCheckMinimumRequiredProps;
//# sourceMappingURL=checkMinimumRequiredProps.js.map