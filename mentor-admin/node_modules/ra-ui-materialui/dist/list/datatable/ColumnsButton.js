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
import { useTranslate, useResourceContext } from 'ra-core';
import { Button, useMediaQuery, Tooltip, IconButton, Popover, Box, } from '@mui/material';
import { useRtl } from '@mui/system/RtlProvider';
import ViewWeekIcon from '@mui/icons-material/ViewWeek';
import { styled, useThemeProps, } from '@mui/material/styles';
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
export var ColumnsButton = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var resource = useResourceContext(props);
    var storeKey = props.storeKey || "".concat(resource, ".datatable");
    var _a = React.useState(null), anchorEl = _a[0], setAnchorEl = _a[1];
    var isRtl = useRtl();
    var translate = useTranslate();
    var isXSmall = useMediaQuery(function (theme) {
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
        isXSmall ? (React.createElement(Tooltip, { title: title },
            React.createElement(IconButton, __assign({ "aria-label": title, color: "primary", onClick: handleClick, size: "large" }, sanitizeRestProps(props)),
                React.createElement(ViewWeekIcon, null)))) : (React.createElement(Button, __assign({ size: "small", onClick: handleClick, startIcon: React.createElement(ViewWeekIcon, null) }, sanitizeRestProps(props)), title)),
        React.createElement(Popover, { open: Boolean(anchorEl), keepMounted: true, anchorEl: anchorEl, onClose: handleClose, anchorOrigin: {
                vertical: 'bottom',
                horizontal: isRtl ? 'right' : 'left',
            }, transformOrigin: isRtl ? RTL_ORIGIN : LTR_ORIGIN },
            React.createElement(Box, { id: "".concat(storeKey, "-columnsSelector"), sx: { px: 1, my: 0, minWidth: 200 } }))));
};
var RTL_ORIGIN = {
    vertical: 'top',
    horizontal: 'right',
};
var LTR_ORIGIN = {
    vertical: 'top',
    horizontal: 'left',
};
var PREFIX = 'RaColumnsButton';
var Root = styled('span', {
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