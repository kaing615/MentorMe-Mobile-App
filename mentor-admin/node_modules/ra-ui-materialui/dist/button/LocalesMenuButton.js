import * as React from 'react';
import { useState } from 'react';
import { useLocaleState, useLocales } from 'ra-core';
import { Button, Menu, MenuItem, styled, useThemeProps, } from '@mui/material';
import LanguageIcon from '@mui/icons-material/Translate';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
/**
 * Language selector. Changes the locale in the app and persists it in
 * preferences so that the app opens with the right locale in the future.
 *
 * Uses i18nProvider.getLocales() to get the list of available locales.
 *
 * @example
 * import { AppBar, TitlePortal, LocalesMenuButton } from 'react-admin';
 *
 * const MyAppBar = () => (
 *     <AppBar>
 *         <TitlePortal />
 *         <LocalesMenuButton />
 *     </AppBar>
 * );
 */
export var LocalesMenuButton = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var _a = props.icon, icon = _a === void 0 ? DefaultIcon : _a, languagesProp = props.languages;
    var _b = useState(null), anchorEl = _b[0], setAnchorEl = _b[1];
    var languages = useLocales({ locales: languagesProp });
    var _c = useLocaleState(), locale = _c[0], setLocale = _c[1];
    var getNameForLocale = function (locale) {
        var language = languages.find(function (language) { return language.locale === locale; });
        return language ? language.name : '';
    };
    var changeLocale = function (locale) { return function () {
        setLocale(locale);
        setAnchorEl(null);
    }; };
    var handleLanguageClick = function (event) {
        setAnchorEl(event.currentTarget);
    };
    var handleClose = function () {
        setAnchorEl(null);
    };
    return (React.createElement(Root, { component: "span" },
        React.createElement(Button, { color: "inherit", variant: "text", "aria-controls": "simple-menu", "aria-label": "", "aria-haspopup": "true", onClick: handleLanguageClick, startIcon: icon, endIcon: React.createElement(ExpandMoreIcon, { fontSize: "small" }) }, getNameForLocale(locale)),
        React.createElement(Menu, { id: "simple-menu", anchorEl: anchorEl, keepMounted: true, open: Boolean(anchorEl), onClose: handleClose }, languages.map(function (language) { return (React.createElement(MenuItem, { key: language.locale, onClick: changeLocale(language.locale), selected: language.locale === locale }, language.name)); }))));
};
var DefaultIcon = React.createElement(LanguageIcon, null);
var PREFIX = 'RaLocalesMenuButton';
export var LocalesMenuButtonClasses = {};
var Root = styled('span', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})({});
//# sourceMappingURL=LocalesMenuButton.js.map