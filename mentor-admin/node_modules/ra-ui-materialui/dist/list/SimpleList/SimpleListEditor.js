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
import * as React from 'react';
import { TextField } from '@mui/material';
import { useTranslate, usePreferenceInput, useSetInspectorTitle, } from 'ra-core';
/**
 * A component which provides a configuration UI to tweak the SimpleList
 *
 * @param {SimpleListEditorProps} props
 * @param props.defaultPrimaryText The SimpleList columns
 * @param {String} props.resource The resource
 * @param {String} props.preferenceKey The key of the columns preferences
 */
export var SimpleListEditor = function (props) {
    var _a = props.defaultPrimaryText, defaultPrimaryText = _a === void 0 ? '' : _a, _b = props.defaultSecondaryText, defaultSecondaryText = _b === void 0 ? '' : _b, _c = props.defaultTertiatyText, defaultTertiatyText = _c === void 0 ? '' : _c;
    useSetInspectorTitle('ra.inspector.SimpleList.title', { _: 'List' });
    var translate = useTranslate();
    var primaryTextField = usePreferenceInput('primaryText', defaultPrimaryText);
    var secondaryTextField = usePreferenceInput('secondaryText', defaultSecondaryText);
    var tertiaryTextField = usePreferenceInput('tertiaryText', defaultTertiatyText);
    return (React.createElement("form", null,
        React.createElement(TextField, __assign({ label: translate('ra.configurable.SimpleList.primaryText', {
                _: 'Primary Text',
            }) }, primaryTextField, { variant: "filled", size: "small", fullWidth: true, sx: { mb: 1 } })),
        React.createElement(TextField, __assign({ label: translate('ra.configurable.SimpleList.secondaryText', {
                _: 'Secondary Text',
            }) }, secondaryTextField, { variant: "filled", size: "small", fullWidth: true, sx: { mb: 1 } })),
        React.createElement(TextField, __assign({ label: translate('ra.configurable.SimpleList.tertiaryText', {
                _: 'Tertiary Text',
            }) }, tertiaryTextField, { variant: "filled", size: "small", fullWidth: true, sx: { mb: 1 } }))));
};
//# sourceMappingURL=SimpleListEditor.js.map