import defaultMessages from 'ra-language-english';
import polyglotI18nProvider from 'ra-i18n-polyglot';
export var defaultI18nProvider = polyglotI18nProvider(function () { return defaultMessages; }, 'en', [{ name: 'en', value: 'English' }], { allowMissing: true });
//# sourceMappingURL=defaultI18nProvider.js.map