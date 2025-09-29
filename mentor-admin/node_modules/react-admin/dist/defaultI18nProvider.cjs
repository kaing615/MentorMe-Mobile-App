"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.defaultI18nProvider = void 0;
var ra_language_english_1 = __importDefault(require("ra-language-english"));
var ra_i18n_polyglot_1 = __importDefault(require("ra-i18n-polyglot"));
exports.defaultI18nProvider = (0, ra_i18n_polyglot_1.default)(function () { return ra_language_english_1.default; }, 'en', [{ name: 'en', value: 'English' }], { allowMissing: true });
//# sourceMappingURL=defaultI18nProvider.js.map