/**
 * Resolve the browser locale according to the value of the global window.navigator
 *
 * Use it to determine the <Admin> locale at runtime.
 *
 * @example
 *    import * as React from "react";
 *    import { Admin, Resource, resolveBrowserLocale } from 'react-admin';
 *    import polyglotI18nProvider from 'ra-i18n-polyglot';
 *    import englishMessages from 'ra-language-english';
 *    import frenchMessages from 'ra-language-french';
 *
 *    const messages = {
 *        fr: frenchMessages,
 *        en: englishMessages,
 *    };
 *    const i18nProvider = polyglotI18nProvider(
 *        locale => messages[locale] ? messages[locale] : messages.en,
 *        resolveBrowserLocale()
 *    );
 *
 *    const App = () => (
 *        <Admin i18nProvider={i18nProvider}>
 *            ...
 *        </Admin>
 *    );
 *
 * @param {string} defaultLocale Defaults to 'en'
 * @param options
 * @param {boolean} options.fullLocale Defaults to false
 */
export declare const resolveBrowserLocale: (defaultLocale?: string, options?: {
    fullLocale?: boolean;
}) => string;
/**
 * Compose translations from multiple packages for a single language (eg: 'english').
 *
 * Use it to merge translations from addons with the main react-admin translations.
 *
 * @example
 *     import * as React from "react";
 *     import { Admin, Resource, mergeTranslations } from 'react-admin';
 *     import polyglotI18nProvider from 'ra-i18n-polyglot';
 *     import englishMessages from 'ra-language-english';
 *     import englishTreeMessages from 'ra-tree-language-english';
 *
 *     const messages = {
 *        en: mergeTranslations(englishMessages, englishTreeMessages),
 *     };
 *     const i18nProvider = polyglotI18nProvider(
 *        locale => messages[locale] ? messages[locale] : messages.en
 *    );
 *
 *    const App = () => (
 *        <Admin i18nProvider={i18nProvider}>
 *            ...
 *        </Admin>
 *    );
 */
export declare const mergeTranslations: (...translationsModules: object[]) => any;
//# sourceMappingURL=TranslationUtils.d.ts.map