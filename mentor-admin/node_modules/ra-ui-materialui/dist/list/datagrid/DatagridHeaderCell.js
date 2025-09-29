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
var _a;
import * as React from 'react';
import { styled, useThemeProps, } from '@mui/material/styles';
import { memo } from 'react';
import clsx from 'clsx';
import { TableCell, TableSortLabel, Tooltip, } from '@mui/material';
import { FieldTitle, useTranslate, useResourceContext, useTranslateLabel, } from 'ra-core';
var oppositeOrder = {
    ASC: 'DESC',
    DESC: 'ASC',
};
export var DatagridHeaderCell = function (inProps) {
    var _a;
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var className = props.className, field = props.field, sort = props.sort, updateSort = props.updateSort, isSorting = props.isSorting, rest = __rest(props, ["className", "field", "sort", "updateSort", "isSorting"]);
    var resource = useResourceContext();
    var translate = useTranslate();
    var translateLabel = useTranslateLabel();
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
    return (React.createElement(StyledTableCell, __assign({ className: clsx(className, field === null || field === void 0 ? void 0 : field.props.headerClassName), align: (field === null || field === void 0 ? void 0 : field.props.textAlign) || (field === null || field === void 0 ? void 0 : field.type.textAlign), variant: "head" }, rest), updateSort &&
        sort &&
        field &&
        field.props.sortable !== false &&
        field.type.sortable !== false &&
        (field.props.sortBy || field.props.source) ? (React.createElement(Tooltip, { title: sortLabel, placement: field.props.textAlign === 'right' ||
            field.type.textAlign === 'right'
            ? 'bottom-end'
            : 'bottom-start', enterDelay: 300 },
        React.createElement(TableSortLabel, { active: sort.field ===
                (field.props.sortBy || field.props.source), direction: sort.order === 'ASC' ? 'asc' : 'desc', "data-field": field.props.sortBy || field.props.source, "data-order": field.props.sortByOrder || 'ASC', onClick: updateSort, classes: DatagridHeaderCellClasses },
            React.createElement(FieldTitle, { label: field.props.label, source: field.props.source, resource: resource })))) : (React.createElement(FieldTitle, { label: field === null || field === void 0 ? void 0 : field.props.label, source: field === null || field === void 0 ? void 0 : field.props.source, resource: resource }))));
};
export default memo(DatagridHeaderCell, function (props, nextProps) {
    var _a, _b, _c, _d;
    return props.updateSort === nextProps.updateSort &&
        ((_a = props.sort) === null || _a === void 0 ? void 0 : _a.field) === ((_b = nextProps.sort) === null || _b === void 0 ? void 0 : _b.field) &&
        ((_c = props.sort) === null || _c === void 0 ? void 0 : _c.order) === ((_d = nextProps.sort) === null || _d === void 0 ? void 0 : _d.order) &&
        props.isSorting === nextProps.isSorting;
});
var PREFIX = 'RaDatagridHeaderCell';
export var DatagridHeaderCellClasses = {
    icon: "".concat(PREFIX, "-icon"),
};
// Remove the sort icons when not active
var StyledTableCell = styled(TableCell, {
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