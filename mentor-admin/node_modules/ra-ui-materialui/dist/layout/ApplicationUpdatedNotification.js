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
import { Alert, Button } from '@mui/material';
import { useTranslate } from 'ra-core';
export var ApplicationUpdatedNotification = React.forwardRef(function (props, ref) {
    var ButtonProps = props.ButtonProps, _a = props.updateText, updateText = _a === void 0 ? 'ra.action.update_application' : _a, _b = props.notificationText, notificationText = _b === void 0 ? 'ra.notification.application_update_available' : _b, alertProps = __rest(props, ["ButtonProps", "updateText", "notificationText"]);
    var translate = useTranslate();
    var handleButtonClick = function () {
        window.location.reload();
    };
    return (React.createElement(Alert, __assign({ ref: ref, severity: "info", action: React.createElement(Button, __assign({ color: "inherit", size: "small", onClick: handleButtonClick }, ButtonProps), translate(updateText, { _: updateText })) }, alertProps), translate(notificationText, { _: notificationText })));
});
//# sourceMappingURL=ApplicationUpdatedNotification.js.map