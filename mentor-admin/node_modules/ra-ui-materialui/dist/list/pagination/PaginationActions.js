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
import * as React from 'react';
import { memo } from 'react';
import { styled, useThemeProps, } from '@mui/material/styles';
import { Pagination } from '@mui/material';
import { useTranslate } from 'ra-core';
export var PaginationActions = memo(function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var page = props.page, rowsPerPage = props.rowsPerPage, count = props.count, onPageChange = props.onPageChange, _a = props.size, size = _a === void 0 ? 'small' : _a, className = props.className, rest = __rest(props, ["page", "rowsPerPage", "count", "onPageChange", "size", "className"]);
    var translate = useTranslate();
    var nbPages = Math.ceil(count / rowsPerPage) || 1;
    if (nbPages === 1) {
        return React.createElement(Root, { className: className });
    }
    var getItemAriaLabel = function (type, page, selected) {
        if (type === 'page') {
            return selected
                ? translate('ra.navigation.current_page', {
                    page: page,
                    _: "page ".concat(page),
                })
                : translate('ra.navigation.page', {
                    page: page,
                    _: "Go to page ".concat(page),
                });
        }
        return translate("ra.navigation.".concat(type), { _: "Go to ".concat(type, " page") });
    };
    return (React.createElement(Root, { className: className },
        React.createElement(Pagination, __assign({ size: size, count: nbPages, 
            // <TablePagination>, the parent, uses 0-based pagination
            // while <Pagination> uses 1-based pagination
            page: page + 1, onChange: function (e, page) { return onPageChange(e, page - 1); } }, sanitizeRestProps(rest), { getItemAriaLabel: getItemAriaLabel }))));
});
var PREFIX = 'RaPaginationActions';
var Root = styled('div', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function () { return ({
    flexShrink: 0,
    ml: 4,
}); });
var sanitizeRestProps = function (_a) {
    var nextIconButtonProps = _a.nextIconButtonProps, backIconButtonProps = _a.backIconButtonProps, slotProps = _a.slotProps, rest = __rest(_a, ["nextIconButtonProps", "backIconButtonProps", "slotProps"]);
    return rest;
};
//# sourceMappingURL=PaginationActions.js.map