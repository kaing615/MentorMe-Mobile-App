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
import { useMemo } from 'react';
import { styled, useThemeProps, } from '@mui/material/styles';
import { Stack } from '@mui/material';
import clsx from 'clsx';
import { FormGroupContextProvider, RecordContextProvider, SourceContextProvider, useRecordContext, useSourceContext, useTranslatableContext, } from 'ra-core';
/**
 * Default container for a group of translatable inputs inside a TranslatableInputs component.
 * @see TranslatableInputs
 */
export var TranslatableInputsTabContent = function (inProps) {
    var _a;
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var children = props.children, _b = props.groupKey, groupKey = _b === void 0 ? '' : _b, locale = props.locale, other = __rest(props, ["children", "groupKey", "locale"]);
    var _c = useTranslatableContext(), selectedLocale = _c.selectedLocale, getRecordForLocale = _c.getRecordForLocale;
    var parentSourceContext = useSourceContext();
    var record = useRecordContext(props);
    // The SourceContext will be read by children of TranslatableInputs to compute their composed source and label
    //
    // <TranslatableInputs locales={['en', 'fr']} /> => SourceContext is "fr"
    //     <TextInput source="description" /> => final source for this input will be "description.fr"
    // </TranslatableInputs>
    var sourceContext = useMemo(function () { return ({
        getSource: function (source) {
            if (!source) {
                throw new Error('Children of TranslatableInputs must have a source');
            }
            return parentSourceContext.getSource("".concat(source, ".").concat(locale));
        },
        getLabel: function (source) {
            return parentSourceContext.getLabel(source);
        },
    }); }, [locale, parentSourceContext]);
    // As fields rely on the RecordContext to get their values and have no knowledge of the locale,
    // we need to create a new record with the values for the current locale only
    // Given the record { title: { en: 'title_en', fr: 'title_fr' } } and the locale 'fr',
    // the record for the locale 'fr' will be { title: 'title_fr' }
    var recordForLocale = useMemo(function () { return getRecordForLocale(record, locale); }, [getRecordForLocale, record, locale]);
    return (React.createElement(FormGroupContextProvider, { name: "".concat(groupKey).concat(locale) },
        React.createElement(Root, __assign({ role: "tabpanel", id: "translatable-content-".concat(groupKey).concat(locale), "aria-labelledby": "translatable-header-".concat(groupKey).concat(locale), className: clsx(TranslatableInputsTabContentClasses.root, (_a = {},
                _a[TranslatableInputsTabContentClasses.hidden] = selectedLocale !== locale,
                _a)) }, other),
            React.createElement(SourceContextProvider, { value: sourceContext },
                React.createElement(RecordContextProvider, { value: recordForLocale }, children)))));
};
var PREFIX = 'RaTranslatableInputsTabContent';
export var TranslatableInputsTabContentClasses = {
    root: "".concat(PREFIX, "-root"),
    hidden: "".concat(PREFIX, "-hidden"),
};
var Root = styled(Stack, { name: PREFIX })(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["&.".concat(TranslatableInputsTabContentClasses.root)] = {
            flexGrow: 1,
            paddingLeft: theme.spacing(2),
            paddingRight: theme.spacing(2),
            paddingTop: theme.spacing(1),
            paddingBottom: theme.spacing(1),
            borderRadius: 0,
            borderBottomLeftRadius: theme.shape.borderRadius,
            borderBottomRightRadius: theme.shape.borderRadius,
            border: "1px solid ".concat((theme.vars || theme).palette.divider),
            borderTop: 0,
        },
        _b["&.".concat(TranslatableInputsTabContentClasses.hidden)] = {
            display: 'none',
        },
        _b);
});
//# sourceMappingURL=TranslatableInputsTabContent.js.map