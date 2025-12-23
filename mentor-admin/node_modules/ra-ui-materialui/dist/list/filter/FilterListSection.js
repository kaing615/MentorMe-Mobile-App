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
import { Box, Typography } from '@mui/material';
import { useTranslate } from 'ra-core';
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
export var FilterListSection = function (props) {
    var label = props.label, icon = props.icon, children = props.children, rest = __rest(props, ["label", "icon", "children"]);
    var translate = useTranslate();
    return (React.createElement(Box, __assign({}, rest),
        React.createElement(Box, { sx: {
                mt: 2,
                display: 'flex',
                alignItems: 'center',
            } },
            React.createElement(Box, { sx: {
                    mr: 1,
                    lineHeight: 'initial',
                } }, icon),
            React.createElement(Typography, { variant: "overline" }, translate(label, { _: label }))),
        children));
};
//# sourceMappingURL=FilterListSection.js.map