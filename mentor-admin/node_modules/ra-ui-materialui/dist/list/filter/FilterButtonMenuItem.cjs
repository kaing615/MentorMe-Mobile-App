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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.FilterButtonMenuItem = void 0;
var React = __importStar(require("react"));
var react_1 = require("react");
var material_1 = require("@mui/material");
var CheckBoxOutlineBlank_1 = __importDefault(require("@mui/icons-material/CheckBoxOutlineBlank"));
var CheckBox_1 = __importDefault(require("@mui/icons-material/CheckBox"));
var ra_core_1 = require("ra-core");
exports.FilterButtonMenuItem = (0, react_1.forwardRef)(function (props, ref) {
    var filter = props.filter, onShow = props.onShow, onHide = props.onHide, autoFocus = props.autoFocus, displayed = props.displayed;
    var resource = (0, ra_core_1.useResourceContext)(props);
    var handleShow = (0, react_1.useCallback)(function () {
        onShow({
            source: filter.props.source,
            defaultValue: filter.props.defaultValue,
        });
    }, [filter.props.defaultValue, filter.props.source, onShow]);
    var handleHide = (0, react_1.useCallback)(function () {
        onHide({
            source: filter.props.source,
        });
    }, [filter.props.source, onHide]);
    return (React.createElement(material_1.MenuItem, { className: "new-filter-item", "data-key": filter.props.source, "data-default-value": filter.props.defaultValue, key: filter.props.source, onClick: displayed ? handleHide : handleShow, autoFocus: autoFocus, ref: ref, disabled: filter.props.disabled, role: "menuitemcheckbox", "aria-checked": displayed },
        React.createElement(material_1.ListItemIcon, null, displayed ? (React.createElement(CheckBox_1.default, { fontSize: "small" })) : (React.createElement(CheckBoxOutlineBlank_1.default, { fontSize: "small" }))),
        React.createElement(material_1.ListItemText, null,
            React.createElement(ra_core_1.FieldTitle, { label: filter.props.label, source: filter.props.source, resource: resource }))));
});
//# sourceMappingURL=FilterButtonMenuItem.js.map