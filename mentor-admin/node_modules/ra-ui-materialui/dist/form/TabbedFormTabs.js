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
import { Children, cloneElement, isValidElement } from 'react';
import Tabs from '@mui/material/Tabs';
import { useParams } from 'react-router-dom';
export var TabbedFormTabs = function (props) {
    var children = props.children, url = props.url, syncWithLocation = props.syncWithLocation, value = props.value, rest = __rest(props, ["children", "url", "syncWithLocation", "value"]);
    var params = useParams();
    // params will include eventual parameters from the root pathname and * for the remaining part
    // which should match the tabs paths
    var tabValue = params['*'];
    return (React.createElement(Tabs, __assign({ value: syncWithLocation ? tabValue : value, indicatorColor: "primary" }, rest), Children.map(children, function (tab, index) {
        if (!isValidElement(tab))
            return null;
        // Builds the full tab which is the concatenation of the last matched route in the
        // TabbedShowLayout hierarchy (ex: '/posts/create', '/posts/12', , '/posts/12/show')
        // and the tab path.
        // This will be used as the Tab's value
        var tabPath = getTabbedFormTabFullPath(tab, index);
        return cloneElement(tab, {
            intent: 'header',
            value: syncWithLocation ? tabPath : index,
            syncWithLocation: syncWithLocation,
        });
    })));
};
export var getTabbedFormTabFullPath = function (tab, index) {
    return tab.props.path != null ? tab.props.path : index > 0 ? index.toString() : '';
};
//# sourceMappingURL=TabbedFormTabs.js.map