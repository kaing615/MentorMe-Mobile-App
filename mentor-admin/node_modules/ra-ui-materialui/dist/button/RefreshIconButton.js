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
import { useCallback } from 'react';
import Tooltip from '@mui/material/Tooltip';
import IconButton from '@mui/material/IconButton';
import NavigationRefresh from '@mui/icons-material/Refresh';
import { useRefresh, useTranslate } from 'ra-core';
export var RefreshIconButton = function (props) {
    var _a = props.label, label = _a === void 0 ? 'ra.action.refresh' : _a, _b = props.icon, icon = _b === void 0 ? defaultIcon : _b, onClick = props.onClick, className = props.className, rest = __rest(props, ["label", "icon", "onClick", "className"]);
    var refresh = useRefresh();
    var translate = useTranslate();
    var handleClick = useCallback(function (event) {
        event.preventDefault();
        refresh();
        if (typeof onClick === 'function') {
            onClick(event);
        }
    }, [refresh, onClick]);
    return (React.createElement(Tooltip, { title: label && translate(label, { _: 'Refresh' }) },
        React.createElement(IconButton, __assign({ "aria-label": label && translate(label, { _: 'Refresh' }), className: className, color: "inherit", onClick: handleClick }, rest), icon)));
};
var defaultIcon = React.createElement(NavigationRefresh, null);
//# sourceMappingURL=RefreshIconButton.js.map