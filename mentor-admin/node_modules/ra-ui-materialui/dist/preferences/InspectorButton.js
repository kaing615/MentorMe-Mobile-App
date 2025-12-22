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
import { IconButton, Tooltip } from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';
import { useTranslate, usePreferencesEditor } from 'ra-core';
export var InspectorButton = React.forwardRef(function (_a, ref) {
    var _b = _a.label, label = _b === void 0 ? 'ra.configurable.configureMode' : _b, _c = _a.SvgIconProps, SvgIconProps = _c === void 0 ? {} : _c, props = __rest(_a, ["label", "SvgIconProps"]);
    var _d = usePreferencesEditor(), enable = _d.enable, disable = _d.disable, setPreferenceKey = _d.setPreferenceKey, isEnabled = _d.isEnabled;
    var translate = useTranslate();
    var handleClick = function () {
        if (isEnabled) {
            disable();
            setPreferenceKey(null);
        }
        else {
            enable();
        }
    };
    var translatedLabel = translate(label, { _: 'Configure mode' });
    return (React.createElement(Tooltip, { title: translatedLabel },
        React.createElement(IconButton, __assign({ "aria-label": translatedLabel, onClick: handleClick, color: "inherit", ref: ref }, props),
            React.createElement(SettingsIcon, __assign({ fontSize: "inherit" }, SvgIconProps)))));
});
//# sourceMappingURL=InspectorButton.js.map