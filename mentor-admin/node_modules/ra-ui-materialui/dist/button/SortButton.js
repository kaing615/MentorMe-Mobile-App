var _a;
import * as React from 'react';
import { memo } from 'react';
import clsx from 'clsx';
import { Button, Menu, MenuItem, Tooltip, IconButton, useMediaQuery, } from '@mui/material';
import { styled, useThemeProps, } from '@mui/material/styles';
import SortIcon from '@mui/icons-material/Sort';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import { useListSortContext, useTranslate, useTranslateLabel, shallowEqual, } from 'ra-core';
/**
 * A button allowing to change the sort field and order.
 *
 * To be used inside a ListContext (e.g. inside a <List> or <ReferenceManyField>)
 *
 * Expects one 'fields' prop, containing an array of field strings that shall
 * be used and displayed for sorting.
 *
 * When users clicks on the <SortButton>, they see a dropdown list with the
 * proposed sorting fields. Once they click on one of these fields, the related
 * list refreshes, re-sorted.
 *
 * @example
 *
 * import * as React from 'react';
 * import { TopToolbar, SortButton, CreateButton, ExportButton } from 'react-admin';
 *
 * const ListActions = () => (
 *     <TopToolbar>
 *         <SortButton fields={['reference', 'sales', 'stock']} />
 *         <CreateButton />
 *         <ExportButton />
 *     </TopToolbar>
 * );
 */
var SortButton = function (inProps) {
    var props = useThemeProps({
        name: PREFIX,
        props: inProps,
    });
    var fields = props.fields, _a = props.label, label = _a === void 0 ? 'ra.sort.sort_by' : _a, _b = props.icon, icon = _b === void 0 ? defaultIcon : _b, sx = props.sx, className = props.className, resourceProp = props.resource;
    var _c = useListSortContext(), resourceFromContext = _c.resource, sort = _c.sort, setSort = _c.setSort;
    var resource = resourceProp || resourceFromContext;
    var translate = useTranslate();
    var translateLabel = useTranslateLabel();
    var isXSmall = useMediaQuery(function (theme) {
        return theme.breakpoints.down('sm');
    });
    var _d = React.useState(null), anchorEl = _d[0], setAnchorEl = _d[1];
    var handleClick = function (event) {
        setAnchorEl(event.currentTarget);
    };
    var handleClose = function () {
        setAnchorEl(null);
    };
    var handleChangeSort = function (event) {
        var field = event.currentTarget.dataset.sort;
        if (!field) {
            throw new Error('<SortButton> MenuItems should have a data-sort attribute');
        }
        setSort({
            field: field,
            order: field === sort.field ? inverseOrder(sort.order) : 'ASC',
        });
        setAnchorEl(null);
    };
    var fieldLabel = translateLabel({
        resource: resource,
        source: sort.field,
    });
    var buttonLabel = translate(label, {
        field: fieldLabel,
        field_lower_first: typeof fieldLabel === 'string'
            ? fieldLabel.charAt(0).toLowerCase() + fieldLabel.slice(1)
            : undefined,
        order: translate("ra.sort.".concat(sort.order)),
        _: label,
    });
    return (React.createElement(Root, { sx: sx, className: clsx(className, classNames.root) },
        isXSmall ? (React.createElement(Tooltip, { title: buttonLabel },
            React.createElement(IconButton, { "aria-label": buttonLabel, color: "primary", onClick: handleClick, size: "large" }, icon))) : (React.createElement(Button, { "aria-controls": "simple-menu", "aria-haspopup": "true", color: "primary", onClick: handleClick, startIcon: icon, endIcon: React.createElement(ArrowDropDownIcon, null), size: "small" }, buttonLabel)),
        React.createElement(Menu, { id: "simple-menu", anchorEl: anchorEl, keepMounted: true, open: Boolean(anchorEl), onClose: handleClose }, fields.map(function (field) { return (React.createElement(MenuItem, { onClick: handleChangeSort, "data-sort": field, key: field },
            translateLabel({
                resource: resource,
                source: field,
            }),
            ' ',
            translate("ra.sort.".concat(sort.field === field
                ? inverseOrder(sort.order)
                : 'ASC')))); }))));
};
var defaultIcon = React.createElement(SortIcon, null);
var inverseOrder = function (sort) { return (sort === 'ASC' ? 'DESC' : 'ASC'); };
var arePropsEqual = function (prevProps, nextProps) {
    return shallowEqual(prevProps.fields, nextProps.fields);
};
var PREFIX = 'RaSortButton';
var classNames = {
    root: "".concat(PREFIX, "-root"),
};
var Root = styled('span', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})((_a = {},
    _a[".".concat(classNames.root)] = {},
    _a['& .MuiButton-sizeSmall'] = {
        // fix for icon misalignment on small buttons, see https://github.com/mui/material-ui/pull/30240
        lineHeight: 1.5,
    },
    _a['& .MuiButton-endIcon'] = { ml: 0 },
    _a));
export default memo(SortButton, arePropsEqual);
//# sourceMappingURL=SortButton.js.map