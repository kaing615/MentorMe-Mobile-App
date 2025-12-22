var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
import * as React from 'react';
import { ListItem, ListItemButton } from '@mui/material';
import { useEvent, useGetPathForRecord, useGetPathForRecordCallback, useRecordContext, useResourceContext, } from 'ra-core';
import { Link, useNavigate } from 'react-router-dom';
export var SimpleListItem = function (props) {
    var children = props.children, linkType = props.linkType, rowClick = props.rowClick, rowIndex = props.rowIndex, rowSx = props.rowSx, rowStyle = props.rowStyle;
    var resource = useResourceContext(props);
    var record = useRecordContext(props);
    var navigate = useNavigate();
    // If we don't have a function to get the path, we can compute the path immediately and set the href
    // on the Link correctly without onClick (better for accessibility)
    var isFunctionLink = typeof linkType === 'function' || typeof rowClick === 'function';
    var pathForRecord = useGetPathForRecord({
        link: isFunctionLink ? false : linkType !== null && linkType !== void 0 ? linkType : rowClick,
        resource: resource,
    });
    var getPathForRecord = useGetPathForRecordCallback();
    var handleClick = useEvent(function () { return __awaiter(void 0, void 0, void 0, function () {
        var link, path;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    // No need to handle non function linkType or rowClick
                    if (!isFunctionLink)
                        return [2 /*return*/];
                    if (!record)
                        return [2 /*return*/];
                    link = typeof linkType === 'function'
                        ? linkType(record, record.id)
                        : typeof rowClick === 'function'
                            ? function (record, resource) { return rowClick(record.id, resource, record); }
                            : false;
                    return [4 /*yield*/, getPathForRecord({
                            record: record,
                            resource: resource,
                            link: link,
                        })];
                case 1:
                    path = _a.sent();
                    if (path === false || path == null) {
                        return [2 /*return*/];
                    }
                    navigate(path);
                    return [2 /*return*/];
            }
        });
    }); });
    if (!record)
        return null;
    if (isFunctionLink) {
        return (React.createElement(ListItem, { disablePadding: true, sx: {
                '.MuiListItem-container': {
                    width: '100%',
                },
            } },
            React.createElement(ListItemButton, { onClick: handleClick, style: rowStyle ? rowStyle(record, rowIndex) : undefined, sx: rowSx === null || rowSx === void 0 ? void 0 : rowSx(record, rowIndex) }, children)));
    }
    if (pathForRecord) {
        return (React.createElement(ListItem, { disablePadding: true, sx: {
                '.MuiListItem-container': {
                    width: '100%',
                },
            } },
            React.createElement(ListItemButton, { component: Link, to: pathForRecord, style: rowStyle ? rowStyle(record, rowIndex) : undefined, sx: rowSx === null || rowSx === void 0 ? void 0 : rowSx(record, rowIndex) }, children)));
    }
    return (React.createElement(ListItem, { sx: {
            '.MuiListItem-container': {
                width: '100%',
            },
        } }, children));
};
//# sourceMappingURL=SimpleListItem.js.map