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
import React from 'react';
import { styled, useThemeProps, } from '@mui/material/styles';
import { Tab } from '@mui/material';
import { useFormGroup, useTranslate } from 'ra-core';
import { capitalize } from 'inflection';
import clsx from 'clsx';
/**
 * Single tab that selects a locale in a TranslatableInputs component.
 * @see TranslatableInputs
 */
export var TranslatableInputsTab = function (inProps) {
    var _a;
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var _b = props.groupKey, groupKey = _b === void 0 ? '' : _b, locale = props.locale, rest = __rest(props, ["groupKey", "locale"]);
    var isValid = useFormGroup("".concat(groupKey).concat(locale)).isValid;
    var translate = useTranslate();
    return (React.createElement(StyledTab, __assign({ id: "translatable-header-".concat(groupKey).concat(locale), label: translate("ra.locales.".concat(locale), {
            _: capitalize(locale),
        }), className: clsx(TranslatableInputsTabClasses.root, (_a = {},
            _a[TranslatableInputsTabClasses.error] = !isValid,
            _a)) }, rest)));
};
var PREFIX = 'RaTranslatableInputsTab';
export var TranslatableInputsTabClasses = {
    root: "".concat(PREFIX, "-root"),
    error: "".concat(PREFIX, "-error"),
};
var StyledTab = styled(Tab, { name: PREFIX })(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["&.".concat(TranslatableInputsTabClasses.root)] = {
            fontSize: '0.8em',
            minHeight: theme.spacing(3),
            minWidth: theme.spacing(6),
        },
        _b["&.".concat(TranslatableInputsTabClasses.error)] = {
            color: (theme.vars || theme).palette.error.main,
        },
        _b);
});
//# sourceMappingURL=TranslatableInputsTab.js.map