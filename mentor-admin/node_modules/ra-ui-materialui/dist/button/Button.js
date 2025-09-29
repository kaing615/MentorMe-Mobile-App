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
import { Button as MuiButton, Tooltip, IconButton, useMediaQuery, } from '@mui/material';
import { styled, useThemeProps, } from '@mui/material/styles';
import { useTranslate } from 'ra-core';
/**
 * A generic Button with side icon. Only the icon is displayed on small screens.
 *
 * The component translates the label. Pass the icon as child.
 * The icon displays on the left side of the button by default. Set alignIcon prop to 'right' to inverse.
 *
 * @example
 *
 * <Button label="Edit" color="secondary" onClick={doEdit}>
 *   <ContentCreate />
 * </Button>
 *
 */
export var Button = function (inProps) {
    var props = useThemeProps({ props: inProps, name: PREFIX });
    var _a = props.alignIcon, alignIcon = _a === void 0 ? 'left' : _a, children = props.children, className = props.className, disabled = props.disabled, label = props.label, _b = props.color, color = _b === void 0 ? 'primary' : _b, _c = props.size, size = _c === void 0 ? 'small' : _c, locationDescriptor = props.to, rest = __rest(props, ["alignIcon", "children", "className", "disabled", "label", "color", "size", "to"]);
    var translate = useTranslate();
    var translatedLabel = label;
    if (typeof label === 'string') {
        translatedLabel = translate(label, {
            _: label,
        });
    }
    var linkParams = getLinkParams(locationDescriptor);
    var isXSmall = useMediaQuery(function (theme) {
        return theme.breakpoints.down('sm');
    });
    return isXSmall ? (label && !disabled ? (React.createElement(Tooltip, { title: translatedLabel },
        React.createElement(IconButton
        // If users provide a ReactNode as label, its their responsibility to also provide an aria-label should they need it
        , __assign({ "aria-label": typeof translatedLabel === 'string'
                ? translatedLabel
                : undefined, className: className, color: color, size: "large" }, linkParams, rest), children))) : (React.createElement(IconButton, __assign({ className: className, color: color, disabled: disabled, size: "large" }, linkParams, rest), children))) : (React.createElement(StyledButton, __assign({ className: className, color: color, size: size, "aria-label": typeof translatedLabel === 'string'
            ? translatedLabel
            : undefined, disabled: disabled, startIcon: alignIcon === 'left' && children ? children : undefined, endIcon: alignIcon === 'right' && children ? children : undefined }, linkParams, rest), translatedLabel));
};
var PREFIX = 'RaButton';
var StyledButton = styled(MuiButton, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})({
    '&.MuiButton-sizeSmall': {
        // fix for icon misalignment on small buttons, see https://github.com/mui/material-ui/pull/30240
        lineHeight: 1.5,
    },
});
var getLinkParams = function (locationDescriptor) {
    // eslint-disable-next-line eqeqeq
    if (locationDescriptor == undefined) {
        return undefined;
    }
    if (typeof locationDescriptor === 'string') {
        return { to: locationDescriptor };
    }
    var redirect = locationDescriptor.redirect, replace = locationDescriptor.replace, state = locationDescriptor.state, to = __rest(locationDescriptor, ["redirect", "replace", "state"]);
    return {
        to: to,
        redirect: redirect,
        replace: replace,
        state: state,
    };
};
//# sourceMappingURL=Button.js.map