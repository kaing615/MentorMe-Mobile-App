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
import Polyglot from 'node-polyglot';
/**
 * Build a polyglot-based i18nProvider based on a function returning the messages for a locale
 *
 * @example
 *
 * import { Admin, Resource } from 'react-admin';
 * import polyglotI18nProvider from 'ra-i18n-polyglot';
 * import englishMessages from 'ra-language-english';
 * import frenchMessages from 'ra-language-french';
 *
 * const messages = {
 *     fr: frenchMessages,
 *     en: englishMessages,
 * };
 * const i18nProvider = polyglotI18nProvider(
 *     locale => messages[locale],
 *     'en',
 *     [{ locale: 'en', name: 'English' }, { locale: 'fr', name: 'Français' }]
 * )
 */
export default (function (getMessages, initialLocale, availableLocales, polyglotOptions) {
    if (initialLocale === void 0) { initialLocale = 'en'; }
    if (availableLocales === void 0) { availableLocales = [{ locale: 'en', name: 'English' }]; }
    if (polyglotOptions === void 0) { polyglotOptions = {}; }
    var locale = initialLocale;
    var messages = getMessages(initialLocale);
    if (messages instanceof Promise) {
        throw new Error("The i18nProvider returned a Promise for the messages of the default locale (".concat(initialLocale, "). Please update your i18nProvider to return the messages of the default locale in a synchronous way."));
    }
    var availableLocalesFinal, polyglotOptionsFinal;
    if (Array.isArray(availableLocales)) {
        // third argument is an array of locales
        availableLocalesFinal = availableLocales;
        polyglotOptionsFinal = polyglotOptions;
    }
    else {
        // third argument is the polyglotOptions
        availableLocalesFinal = [{ locale: 'en', name: 'English' }];
        polyglotOptionsFinal = availableLocales;
    }
    var polyglot = new Polyglot(__assign({ locale: locale, phrases: __assign({ '': '' }, messages) }, polyglotOptionsFinal));
    var translate = polyglot.t.bind(polyglot);
    return {
        translate: function (key, options) {
            if (options === void 0) { options = {}; }
            return translate(key, options);
        },
        changeLocale: function (newLocale) {
            // We systematically return a Promise for the messages because
            // getMessages may return a Promise
            return Promise.resolve(getMessages(newLocale)).then(function (messages) {
                locale = newLocale;
                var newPolyglot = new Polyglot(__assign({ locale: newLocale, phrases: __assign({ '': '' }, messages) }, polyglotOptions));
                translate = newPolyglot.t.bind(newPolyglot);
            });
        },
        getLocale: function () { return locale; },
        getLocales: function () { return availableLocalesFinal; },
    };
});
//# sourceMappingURL=index.js.map