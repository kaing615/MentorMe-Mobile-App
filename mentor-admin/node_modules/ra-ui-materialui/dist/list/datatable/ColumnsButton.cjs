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
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ColumnsButton = void 0;
var React = __importStar(require("react"));
var ra_core_1 = require("ra-core");
var material_1 = require("@mui/material");
var RtlProvider_1 = require("@mui/system/RtlProvider");
var ViewWeek_1 = __importDefault(require("@mui/icons-material/ViewWeek"));
var styles_1 = require("@mui/material/styles");
/**
 * Renders a button that lets users show / hide columns in a DataTable
 *
 * @example
 * import { ColumnsButton, DataTable } from 'react-admin';
 *
 * const PostListActions = () => (
 *   <TopToolbar>
        <ColumnsButton />
        <FilterButton />
 *   </TopToolbar>
 * );
 *
 * const PostList = () => (
 *   <List actions={<PostListActions />}>
 *     <DataTable>
 *       <DataTable.Col source="title" />
 *       <DataTable.Col source="author" />
         ...
 *     </DataTable>
 *   </List>
 * );
 */
var ColumnsButton = function (inProps) {
    var props = (0, styles_1.useThemeProps)({
        props: inProps,
        name: PREFIX,
    });
    var resource = (0, ra_core_1.useResourceContext)(props);
    var storeKey = props.storeKey || "".concat(resource, ".datatable");
    var _a = React.useState(null), anchorEl = _a[0], setAnchorEl = _a[1];
    var isRtl = (0, RtlProvider_1.useRtl)();
    var translate = (0, ra_core_1.useTranslate)();
    var isXSmall = (0, material_1.useMediaQuery)(function (theme) {
        return theme.breakpoints.down('sm');
    });
    var title = translate('ra.action.select_columns', { _: 'Columns' });
    var handleClick = function (event) {
        setAnchorEl(event.currentTarget);
    };
    var handleClose = function () {
        setAnchorEl(null);
    };
    return (React.createElement(Root, null,
        isXSmall ? (React.createElement(material_1.Tooltip, { title: title },
            React.createElement(material_1.IconButton, __assign({ "aria-label": title, color: "primary", onClick: handleClick, size: "large" }, sanitizeRestProps(props)),
                React.createElement(ViewWeek_1.default, null)))) : (React.createElement(material_1.Button, __assign({ size: "small", onClick: handleClick, startIcon: React.createElement(ViewWeek_1.default, null) }, sanitizeRestProps(props)), title)),
        React.createElement(material_1.Popover, { open: Boolean(anchorEl), keepMounted: true, anchorEl: anchorEl, onClose: handleClose, anchorOrigin: {
                vertical: 'bottom',
                horizontal: isRtl ? 'right' : 'left',
            }, transformOrigin: isRtl ? RTL_ORIGIN : LTR_ORIGIN },
            React.createElement(material_1.Box, { id: "".concat(storeKey, "-columnsSelector"), sx: { px: 1, my: 0, minWidth: 200 } }))));
};
exports.ColumnsButton = ColumnsButton;
var RTL_ORIGIN = {
    vertical: 'top',
    horizontal: 'right',
};
var LTR_ORIGIN = {
    vertical: 'top',
    horizontal: 'left',
};
var PREFIX = 'RaColumnsButton';
var Root = (0, styles_1.styled)('span', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})({
    '& .MuiButton-sizeSmall': {
        // fix for icon misalignment on small buttons, see https://github.com/mui/material-ui/pull/30240
        lineHeight: 1.5,
    },
});
var sanitizeRestProps = function (_a) {
    var resource = _a.resource, storeKey = _a.storeKey, rest = __rest(_a, ["resource", "storeKey"]);
    return rest;
};
//# sourceMappingURL=ColumnsButton.js.map