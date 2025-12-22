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
var _a;
Object.defineProperty(exports, "__esModule", { value: true });
exports.DatagridHeaderCellClasses = exports.DatagridHeaderCell = void 0;
var React = __importStar(require("react"));
var styles_1 = require("@mui/material/styles");
var react_1 = require("react");
var clsx_1 = __importDefault(require("clsx"));
var material_1 = require("@mui/material");
var ra_core_1 = require("ra-core");
var oppositeOrder = {
    ASC: 'DESC',
    DESC: 'ASC',
};
var DatagridHeaderCell = function (inProps) {
    var _a;
    var props = (0, styles_1.useThemeProps)({
        props: inProps,
        name: PREFIX,
    });
    var className = props.className, field = props.field, sort = props.sort, updateSort = props.updateSort, isSorting = props.isSorting, rest = __rest(props, ["className", "field", "sort", "updateSort", "isSorting"]);
    var resource = (0, ra_core_1.useResourceContext)();
    var translate = (0, ra_core_1.useTranslate)();
    var translateLabel = (0, ra_core_1.useTranslateLabel)();
    var nextSortOrder = field
        ? sort && sort.field === (field.props.sortBy || field.props.source)
            ? // active sort field, use opposite order
                oppositeOrder[sort.order]
            : // non active sort field, use default order
                (_a = field === null || field === void 0 ? void 0 : field.props.sortByOrder) !== null && _a !== void 0 ? _a : 'ASC'
        : undefined;
    var fieldLabel = field
        ? translateLabel({
            label: typeof field.props.label === 'string'
                ? field.props.label
                : undefined,
            resource: resource,
            source: field.props.source,
        })
        : undefined;
    var sortLabel = translate('ra.sort.sort_by', {
        field: fieldLabel,
        field_lower_first: typeof fieldLabel === 'string'
            ? fieldLabel.charAt(0).toLowerCase() + fieldLabel.slice(1)
            : undefined,
        order: translate("ra.sort.".concat(nextSortOrder)),
        _: translate('ra.action.sort'),
    });
    return (React.createElement(StyledTableCell, __assign({ className: (0, clsx_1.default)(className, field === null || field === void 0 ? void 0 : field.props.headerClassName), align: (field === null || field === void 0 ? void 0 : field.props.textAlign) || (field === null || field === void 0 ? void 0 : field.type.textAlign), variant: "head" }, rest), updateSort &&
        sort &&
        field &&
        field.props.sortable !== false &&
        field.type.sortable !== false &&
        (field.props.sortBy || field.props.source) ? (React.createElement(material_1.Tooltip, { title: sortLabel, placement: field.props.textAlign === 'right' ||
            field.type.textAlign === 'right'
            ? 'bottom-end'
            : 'bottom-start', enterDelay: 300 },
        React.createElement(material_1.TableSortLabel, { active: sort.field ===
                (field.props.sortBy || field.props.source), direction: sort.order === 'ASC' ? 'asc' : 'desc', "data-field": field.props.sortBy || field.props.source, "data-order": field.props.sortByOrder || 'ASC', onClick: updateSort, classes: exports.DatagridHeaderCellClasses },
            React.createElement(ra_core_1.FieldTitle, { label: field.props.label, source: field.props.source, resource: resource })))) : (React.createElement(ra_core_1.FieldTitle, { label: field === null || field === void 0 ? void 0 : field.props.label, source: field === null || field === void 0 ? void 0 : field.props.source, resource: resource }))));
};
exports.DatagridHeaderCell = DatagridHeaderCell;
exports.default = (0, react_1.memo)(exports.DatagridHeaderCell, function (props, nextProps) {
    var _a, _b, _c, _d;
    return props.updateSort === nextProps.updateSort &&
        ((_a = props.sort) === null || _a === void 0 ? void 0 : _a.field) === ((_b = nextProps.sort) === null || _b === void 0 ? void 0 : _b.field) &&
        ((_c = props.sort) === null || _c === void 0 ? void 0 : _c.order) === ((_d = nextProps.sort) === null || _d === void 0 ? void 0 : _d.order) &&
        props.isSorting === nextProps.isSorting;
});
var PREFIX = 'RaDatagridHeaderCell';
exports.DatagridHeaderCellClasses = {
    icon: "".concat(PREFIX, "-icon"),
};
// Remove the sort icons when not active
var StyledTableCell = (0, styles_1.styled)(material_1.TableCell, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})((_a = {},
    _a["& .MuiTableSortLabel-icon"] = {
        display: 'none',
    },
    _a["& .Mui-active .MuiTableSortLabel-icon"] = {
        display: 'inline',
    },
    _a));
//# sourceMappingURL=DatagridHeaderCell.js.map