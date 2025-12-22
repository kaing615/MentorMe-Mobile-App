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
exports.Button = void 0;
var React = __importStar(require("react"));
var material_1 = require("@mui/material");
var styles_1 = require("@mui/material/styles");
var ra_core_1 = require("ra-core");
/**
 * A generic Button with side icon. Only the icon is displayed on small screens.
 *
 * The component translates the label. Pass the icon as child.
 * The icon displays on the left side of the button by default. Set alignIcon prop to 'right' to inverse.
 *
 * @example
 *
 * <Button label="Edit" color="secondary" onClick={doEdit}>
 *   <ContentCreate />
 * </Button>
 *
 */
var Button = function (inProps) {
    var props = (0, styles_1.useThemeProps)({ props: inProps, name: PREFIX });
    var _a = props.alignIcon, alignIcon = _a === void 0 ? 'left' : _a, children = props.children, className = props.className, disabled = props.disabled, label = props.label, _b = props.color, color = _b === void 0 ? 'primary' : _b, _c = props.size, size = _c === void 0 ? 'small' : _c, locationDescriptor = props.to, rest = __rest(props, ["alignIcon", "children", "className", "disabled", "label", "color", "size", "to"]);
    var translate = (0, ra_core_1.useTranslate)();
    var translatedLabel = label;
    if (typeof label === 'string') {
        translatedLabel = translate(label, {
            _: label,
        });
    }
    var linkParams = getLinkParams(locationDescriptor);
    var isXSmall = (0, material_1.useMediaQuery)(function (theme) {
        return theme.breakpoints.down('sm');
    });
    return isXSmall ? (label && !disabled ? (React.createElement(material_1.Tooltip, { title: translatedLabel },
        React.createElement(material_1.IconButton
        // If users provide a ReactNode as label, its their responsibility to also provide an aria-label should they need it
        , __assign({ "aria-label": typeof translatedLabel === 'string'
                ? translatedLabel
                : undefined, className: className, color: color, size: "large" }, linkParams, rest), children))) : (React.createElement(material_1.IconButton, __assign({ className: className, color: color, disabled: disabled, size: "large" }, linkParams, rest), children))) : (React.createElement(StyledButton, __assign({ className: className, color: color, size: size, "aria-label": typeof translatedLabel === 'string'
            ? translatedLabel
            : undefined, disabled: disabled, startIcon: alignIcon === 'left' && children ? children : undefined, endIcon: alignIcon === 'right' && children ? children : undefined }, linkParams, rest), translatedLabel));
};
exports.Button = Button;
var PREFIX = 'RaButton';
var StyledButton = (0, styles_1.styled)(material_1.Button, {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})({
    '&.MuiButton-sizeSmall': {
        // fix for icon misalignment on small buttons, see https://github.com/mui/material-ui/pull/30240
        lineHeight: 1.5,
    },
});
var getLinkParams = function (locationDescriptor) {
    // eslint-disable-next-line eqeqeq
    if (locationDescriptor == undefined) {
        return undefined;
    }
    if (typeof locationDescriptor === 'string') {
        return { to: locationDescriptor };
    }
    var redirect = locationDescriptor.redirect, replace = locationDescriptor.replace, state = locationDescriptor.state, to = __rest(locationDescriptor, ["redirect", "replace", "state"]);
    return {
        to: to,
        redirect: redirect,
        replace: replace,
        state: state,
    };
};
//# sourceMappingURL=Button.js.map