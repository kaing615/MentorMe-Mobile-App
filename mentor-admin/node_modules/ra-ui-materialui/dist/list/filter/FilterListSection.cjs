"use strict";
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
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
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
Object.defineProperty(exports, "__esModule", { value: true });
exports.FilterListSection = void 0;
var React = __importStar(require("react"));
var material_1 = require("@mui/material");
var ra_core_1 = require("ra-core");
/**
 * This component offers a wrapper to render children inside a FilterList
 * section.
 *
 * It basically adds a header with an icon and a label, before rendering the
 * children.
 *
 * It is used by `<FilterList>`, but can also be used standalone to make your
 * own components look nicer alongside a filter list.
 *
 * @example
 * import MailIcon from '@mui/icons-material/MailOutline';
 * import TitleIcon from '@mui/icons-material/Title';
 * import { Card, CardContent } from '@mui/material';
 * import * as React from 'react';
 * import {
 *     FilterLiveForm,
 *     FilterList,
 *     FilterListItem,
 *     FilterListSection,
 *     TextInput,
 * } from 'react-admin';
 *
 * export const BookListAside = () => (
 *     <Card sx={{ order: -1, mr: 2, mt: 6, width: 250, height: 'fit-content' }}>
 *         <CardContent>
 *             <FilterList label="Subscribed to newsletter" icon={<MailIcon />}>
 *                 <FilterListItem label="Yes" value={{ has_newsletter: true }} />
 *                 <FilterListItem label="No" value={{ has_newsletter: false }} />
 *             </FilterList>
 *             <FilterListSection label="Title" icon={<TitleIcon />}>
 *                 <FilterLiveForm>
 *                     <TextInput source="title" resettable helperText={false} />
 *                 </FilterLiveForm>
 *             </FilterListSection>
 *         </CardContent>
 *     </Card>
 * );
 */
var FilterListSection = function (props) {
    var label = props.label, icon = props.icon, children = props.children, rest = __rest(props, ["label", "icon", "children"]);
    var translate = (0, ra_core_1.useTranslate)();
    return (React.createElement(material_1.Box, __assign({}, rest),
        React.createElement(material_1.Box, { sx: {
                mt: 2,
                display: 'flex',
                alignItems: 'center',
            } },
            React.createElement(material_1.Box, { sx: {
                    mr: 1,
                    lineHeight: 'initial',
                } }, icon),
            React.createElement(material_1.Typography, { variant: "overline" }, translate(label, { _: label }))),
        children));
};
exports.FilterListSection = FilterListSection;
//# sourceMappingURL=FilterListSection.js.map