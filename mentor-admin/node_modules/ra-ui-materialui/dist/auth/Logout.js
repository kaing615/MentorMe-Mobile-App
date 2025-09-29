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
import { useCallback } from 'react';
import { ListItemIcon, ListItemText, MenuItem, useMediaQuery, } from '@mui/material';
import ExitIcon from '@mui/icons-material/PowerSettingsNew';
import clsx from 'clsx';
import { useTranslate, useLogout, useAuthState } from 'ra-core';
/**
 * Logout button component, to be passed to the Admin component
 *
 * Used for the Logout Menu item in the sidebar
 */
export var Logout = React.forwardRef(function Logout(inProps, ref) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var className = props.className, redirectTo = props.redirectTo, icon = props.icon, rest = __rest(props, ["className", "redirectTo", "icon"]);
    var authenticated = useAuthState().authenticated;
    var isXSmall = useMediaQuery(function (theme) {
        return theme.breakpoints.down('sm');
    });
    var translate = useTranslate();
    var logout = useLogout();
    var handleClick = useCallback(function () { return logout(null, redirectTo, false); }, [redirectTo, logout]);
    if (!authenticated)
        return null;
    return (React.createElement(StyledMenuItem, __assign({ className: clsx('logout', className), onClick: handleClick, ref: ref, component: isXSmall ? 'span' : 'li' }, rest),
        React.createElement(ListItemIcon, { className: LogoutClasses.icon }, icon ? icon : React.createElement(ExitIcon, { fontSize: "small" })),
        React.createElement(ListItemText, null, translate('ra.auth.logout', { _: 'Logout' }))));
});
var PREFIX = 'RaLogout';
export var LogoutClasses = {
    icon: "".concat(PREFIX, "-icon"),
};
var StyledMenuItem = styled(MenuItem, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})((_a = {},
    _a["& .".concat(LogoutClasses.icon)] = {},
    _a));
//# sourceMappingURL=Logout.js.map