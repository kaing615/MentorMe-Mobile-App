"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.valuesAreObject = exports.isObject = exports.valuesAreDateString = exports.isDateString = exports.valuesAreDate = exports.isDate = exports.valuesAreArray = exports.isArray = exports.valuesAreEmail = exports.isEmail = exports.valuesAreImageUrl = exports.isImageUrl = exports.valuesAreUrl = exports.isUrl = exports.valuesAreHtml = exports.isHtml = exports.valuesAreString = exports.isString = exports.valuesAreBooleanString = exports.isBooleanString = exports.valuesAreBoolean = exports.isBoolean = exports.valuesAreInteger = exports.isInteger = exports.valuesAreNumeric = exports.isNumeric = void 0;
var date_fns_1 = require("date-fns");
var isNumeric = function (value) {
    return !isNaN(parseFloat(value)) && isFinite(value);
};
exports.isNumeric = isNumeric;
var valuesAreNumeric = function (values) { return values.every(exports.isNumeric); };
exports.valuesAreNumeric = valuesAreNumeric;
var isInteger = function (value) {
    return Number.isInteger(value) || !isNaN(parseInt(value));
};
exports.isInteger = isInteger;
var valuesAreInteger = function (values) { return values.every(exports.isInteger); };
exports.valuesAreInteger = valuesAreInteger;
var isBoolean = function (value) { return typeof value === 'boolean'; };
exports.isBoolean = isBoolean;
var valuesAreBoolean = function (values) { return values.every(exports.isBoolean); };
exports.valuesAreBoolean = valuesAreBoolean;
var isBooleanString = function (value) {
    return ['true', 'false'].includes(value.toString().toLowerCase());
};
exports.isBooleanString = isBooleanString;
var valuesAreBooleanString = function (values) {
    return values.every(exports.isBooleanString);
};
exports.valuesAreBooleanString = valuesAreBooleanString;
var isString = function (value) { return typeof value === 'string'; };
exports.isString = isString;
var valuesAreString = function (values) { return values.every(exports.isString); };
exports.valuesAreString = valuesAreString;
var HtmlRegexp = /<([A-Z][A-Z0-9]*)\b[^>]*>(.*?)<\/\1>/i;
var isHtml = function (value) { return !value || HtmlRegexp.test(value); };
exports.isHtml = isHtml;
var valuesAreHtml = function (values) { return values.every(exports.isHtml); };
exports.valuesAreHtml = valuesAreHtml;
var UrlRegexp = /http(s*):\/\/.*/i;
var isUrl = function (value) {
    return !value || UrlRegexp.test(value);
};
exports.isUrl = isUrl;
var valuesAreUrl = function (values) { return values.every(exports.isUrl); };
exports.valuesAreUrl = valuesAreUrl;
var ImageUrlRegexp = /^http(s*):\/\/.*\.(jpeg|jpg|jfif|pjpeg|pjp|png|svg|gif|webp|apng|bmp|ico|cur|tif|tiff)/i;
var isImageUrl = function (value) {
    return !value || ImageUrlRegexp.test(value);
};
exports.isImageUrl = isImageUrl;
var valuesAreImageUrl = function (values) { return values.every(exports.isImageUrl); };
exports.valuesAreImageUrl = valuesAreImageUrl;
// This is a very simple regex to find emails
// It is NOT meant to validate emails as the spec is way more complicated but is
// enough for our inference needs
var EmailRegexp = /@{1}/;
var isEmail = function (value) {
    return !value || EmailRegexp.test(value);
};
exports.isEmail = isEmail;
var valuesAreEmail = function (values) { return values.every(exports.isEmail); };
exports.valuesAreEmail = valuesAreEmail;
var isArray = function (value) {
    return Array.isArray(value);
};
exports.isArray = isArray;
var valuesAreArray = function (values) {
    return values.every(exports.isArray);
};
exports.valuesAreArray = valuesAreArray;
var isDate = function (value) {
    return !value || value instanceof Date;
};
exports.isDate = isDate;
var valuesAreDate = function (values) {
    return values.every(exports.isDate);
};
exports.valuesAreDate = valuesAreDate;
var isDateString = function (value) {
    return !value ||
        (typeof value === 'string' &&
            ((0, date_fns_1.isMatch)(value, 'MM/dd/yyyy') ||
                (0, date_fns_1.isMatch)(value, 'MM/dd/yy') ||
                (0, date_fns_1.isValid)((0, date_fns_1.parseISO)(value))));
};
exports.isDateString = isDateString;
var valuesAreDateString = function (values) {
    return values.every(exports.isDateString);
};
exports.valuesAreDateString = valuesAreDateString;
var isObject = function (value) {
    return Object.prototype.toString.call(value) === '[object Object]';
};
exports.isObject = isObject;
var valuesAreObject = function (values) {
    return values.every(exports.isObject);
};
exports.valuesAreObject = valuesAreObject;
//# sourceMappingURL=assertions.js.map