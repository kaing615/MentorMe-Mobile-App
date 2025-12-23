"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.substituteTokens = void 0;
/**
 * Replace tokens by their value in the given string
 *
 * @param {string} template The template with interpolation tokens, e.g. 'Hello, %{name}'
 * @param {object} data The data to interpolate, e.g. { name: 'John' }
 * @returns {string} The interpolated string, e.g. 'Hello, John'
 */
var substituteTokens = function (template, data) {
    return template && data
        ? String.prototype.replace.call(template, defaultTokenRegex, function (expression, argument) {
            if (!data.hasOwnProperty(argument) ||
                data[argument] == null) {
                return expression;
            }
            return data[argument];
        })
        : template;
};
exports.substituteTokens = substituteTokens;
// tokens are like 'Hello, %{name}'
var defaultTokenRegex = /%\{(.*?)\}/g;
//# sourceMappingURL=substituteTokens.js.map