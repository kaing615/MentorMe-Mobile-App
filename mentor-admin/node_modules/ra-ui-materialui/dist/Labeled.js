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
import { Stack, Typography, } from '@mui/material';
import { styled } from '@mui/material/styles';
import clsx from 'clsx';
import { FieldTitle } from 'ra-core';
import { useThemeProps } from '@mui/system';
/**
 * Wrap a field or an input with a label if necessary.
 *
 * The label is displayed if:
 * - the field or input has a label prop that is not false, or
 * - the field or input has a source prop
 *
 * @example
 * <Labeled>
 *     <FooComponent source="title" />
 * </Labeled>
 */
export var Labeled = function (inProps) {
    var _a;
    var _b, _c;
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var children = props.children, _d = props.className, className = _d === void 0 ? '' : _d, color = props.color, _e = props.component, component = _e === void 0 ? 'span' : _e, fullWidth = props.fullWidth, isRequired = props.isRequired, label = props.label, resource = props.resource, source = props.source, TypographyProps = props.TypographyProps, rest = __rest(props, ["children", "className", "color", "component", "fullWidth", "isRequired", "label", "resource", "source", "TypographyProps"]);
    return (React.createElement(Root
    // @ts-ignore https://github.com/mui/material-ui/issues/29875
    , __assign({ 
        // @ts-ignore https://github.com/mui/material-ui/issues/29875
        component: component, className: clsx(className, (_a = {},
            _a[LabeledClasses.fullWidth] = fullWidth,
            _a)) }, rest),
        label !== false &&
            children.props.label !== false &&
            typeof children.type !== 'string' &&
            // @ts-ignore
            ((_b = children.type) === null || _b === void 0 ? void 0 : _b.displayName) !== 'Labeled' &&
            // @ts-ignore
            ((_c = children.type) === null || _c === void 0 ? void 0 : _c.displayName) !== 'Labeled' ? (React.createElement(Typography, __assign({ sx: color
                ? { color: color }
                : {
                    color: function (theme) {
                        return (theme.vars || theme).palette.text
                            .secondary;
                    },
                }, className: LabeledClasses.label }, TypographyProps),
            React.createElement(FieldTitle, { label: label || children.props.label, source: source || children.props.source, resource: resource, isRequired: isRequired }))) : null,
        children));
};
Labeled.displayName = 'Labeled';
var PREFIX = 'RaLabeled';
export var LabeledClasses = {
    label: "".concat(PREFIX, "-label"),
    fullWidth: "".concat(PREFIX, "-fullWidth"),
};
var Root = styled(Stack, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})((_a = {
        display: 'inline-flex',
        marginBottom: '0.2em'
    },
    _a["&.".concat(LabeledClasses.fullWidth)] = {
        width: '100%',
    },
    _a["& .".concat(LabeledClasses.label)] = {
        fontSize: '0.75em',
        marginBottom: '0.2em',
    },
    _a));
//# sourceMappingURL=Labeled.js.map