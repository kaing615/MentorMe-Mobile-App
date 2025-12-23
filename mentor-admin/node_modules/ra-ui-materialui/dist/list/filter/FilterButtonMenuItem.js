import * as React from 'react';
import { forwardRef, useCallback } from 'react';
import { MenuItem, ListItemIcon, ListItemText } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import { FieldTitle, useResourceContext } from 'ra-core';
export var FilterButtonMenuItem = forwardRef(function (props, ref) {
    var filter = props.filter, onShow = props.onShow, onHide = props.onHide, autoFocus = props.autoFocus, displayed = props.displayed;
    var resource = useResourceContext(props);
    var handleShow = useCallback(function () {
        onShow({
            source: filter.props.source,
            defaultValue: filter.props.defaultValue,
        });
    }, [filter.props.defaultValue, filter.props.source, onShow]);
    var handleHide = useCallback(function () {
        onHide({
            source: filter.props.source,
        });
    }, [filter.props.source, onHide]);
    return (React.createElement(MenuItem, { className: "new-filter-item", "data-key": filter.props.source, "data-default-value": filter.props.defaultValue, key: filter.props.source, onClick: displayed ? handleHide : handleShow, autoFocus: autoFocus, ref: ref, disabled: filter.props.disabled, role: "menuitemcheckbox", "aria-checked": displayed },
        React.createElement(ListItemIcon, null, displayed ? (React.createElement(CheckBoxIcon, { fontSize: "small" })) : (React.createElement(CheckBoxOutlineBlankIcon, { fontSize: "small" }))),
        React.createElement(ListItemText, null,
            React.createElement(FieldTitle, { label: filter.props.label, source: filter.props.source, resource: resource }))));
});
//# sourceMappingURL=FilterButtonMenuItem.js.map