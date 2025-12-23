"use strict";
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
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ConfigurableClasses = exports.Configurable = void 0;
var React = __importStar(require("react"));
var react_1 = require("react");
var ra_core_1 = require("ra-core");
var material_1 = require("@mui/material");
var styles_1 = require("@mui/material/styles");
var Settings_1 = __importDefault(require("@mui/icons-material/Settings"));
var clsx_1 = __importDefault(require("clsx"));
/**
 * Wrap any component with this component to make it configurable
 *
 * When the edit mode is enabled, users will see a button to edit the component;
 * when clicked, the inspector will show the editor element.
 *
 * Creates a context for the preference key, so that both the child component
 * and the editor can access it using usePreferenceKey();
 *
 * @example
 * const ConfigurableTextBlock = ({ preferenceKey = "TextBlock", ...props }) => (
 *     <Configurable editor={<TextBlockInspector />} preferenceKey={preferenceKey}>
 *         <TextBlock {...props} />
 *     </Configurable>
 * );
 */
var Configurable = function (inProps) {
    var props = (0, styles_1.useThemeProps)({
        props: inProps,
        name: PREFIX,
    });
    var children = props.children, editor = props.editor, preferenceKey = props.preferenceKey, _a = props.openButtonLabel, openButtonLabel = _a === void 0 ? 'ra.configurable.customize' : _a, sx = props.sx;
    var prefixedPreferenceKey = "preferences.".concat(preferenceKey);
    var preferencesEditorContext = (0, ra_core_1.usePreferencesEditor)();
    var hasPreferencesEditorContext = !!preferencesEditorContext;
    var translate = (0, ra_core_1.useTranslate)();
    var _b = preferencesEditorContext || {}, isEnabled = _b.isEnabled, setEditor = _b.setEditor, currentPreferenceKey = _b.preferenceKey, setPreferenceKey = _b.setPreferenceKey;
    var isEditorOpen = prefixedPreferenceKey === currentPreferenceKey;
    var editorOpenRef = (0, react_1.useRef)(isEditorOpen);
    var wrapperRef = (0, react_1.useRef)(null);
    var _c = (0, react_1.useState)(false), isCustomizeButtonVisible = _c[0], setIsCustomizeButtonVisible = _c[1];
    (0, react_1.useEffect)(function () {
        editorOpenRef.current = isEditorOpen;
    }, [isEditorOpen]);
    // on unmount, if selected, remove the editor
    (0, react_1.useEffect)(function () {
        return function () {
            if (!editorOpenRef.current)
                return;
            setPreferenceKey && setPreferenceKey(null);
            setEditor && setEditor(null);
        };
    }, [setEditor, setPreferenceKey]);
    if (!hasPreferencesEditorContext) {
        return children;
    }
    var handleOpenEditor = function () {
        if (!setEditor) {
            throw new Error('Configurable must be used inside a PreferencesEditorContextProvider');
        }
        // include the editorKey as key to force destroy and mount
        // when switching between two identical editors with different editor keys
        // otherwise the editor will see an update and its useStore will return one tick later
        // which would forbid the usage of uncontrolled inputs in the editor
        setEditor((0, react_1.cloneElement)(editor, {
            preferenceKey: prefixedPreferenceKey,
            key: prefixedPreferenceKey,
        }));
        if (!setPreferenceKey) {
            throw new Error('Configurable must be used inside a PreferencesEditorContextProvider');
        }
        // as we modify the editor, isEditorOpen cannot compare the editor element
        // we'll compare the editor key instead
        setPreferenceKey(prefixedPreferenceKey);
    };
    var handleShowButton = function () {
        setIsCustomizeButtonVisible(true);
    };
    var handleHideButton = function () {
        setIsCustomizeButtonVisible(false);
    };
    return (React.createElement(ra_core_1.PreferenceKeyContextProvider, { value: prefixedPreferenceKey },
        React.createElement(Root, { className: (0, clsx_1.default)(exports.ConfigurableClasses.root, isEnabled && exports.ConfigurableClasses.editMode, isEditorOpen && exports.ConfigurableClasses.editorActive), sx: sx, ref: wrapperRef, onMouseEnter: isEnabled ? handleShowButton : undefined, onMouseLeave: isEnabled ? handleHideButton : undefined }, children),
        React.createElement(material_1.Popover, { open: !!isEnabled && (isCustomizeButtonVisible || isEditorOpen), sx: {
                pointerEvents: 'none',
                '& .MuiPaper-root': {
                    pointerEvents: 'auto',
                    borderRadius: 10,
                    padding: '2px',
                    lineHeight: 0,
                    backgroundColor: 'warning.light',
                    color: 'warning.contrastText',
                    '&:hover': {
                        cursor: 'pointer',
                    },
                },
            }, anchorEl: wrapperRef.current, anchorOrigin: {
                vertical: 'top',
                horizontal: 'right',
            }, transformOrigin: {
                vertical: 'center',
                horizontal: 'center',
            }, onClose: handleHideButton, PaperProps: {
                elevation: 1,
                onMouseEnter: handleShowButton,
                onMouseLeave: handleHideButton,
                title: translate(openButtonLabel),
                onClick: handleOpenEditor,
            }, disableAutoFocus: true, disableRestoreFocus: true, disableEnforceFocus: true, disableScrollLock: true, marginThreshold: 8 },
            React.createElement(Settings_1.default
            // @ts-ignore
            , { 
                // @ts-ignore
                fontSize: "12px" }))));
};
exports.Configurable = Configurable;
var PREFIX = 'RaConfigurable';
exports.ConfigurableClasses = {
    root: "".concat(PREFIX, "-root"),
    editMode: "".concat(PREFIX, "-editMode"),
    editorActive: "".concat(PREFIX, "-editorActive"),
};
var Root = (0, styles_1.styled)('span', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {
            position: 'relative',
            display: 'inline-block'
        },
        _b["&.".concat(exports.ConfigurableClasses.editMode)] = {
            transition: theme.transitions.create('outline'),
            outline: "color-mix(in srgb, ".concat((theme.vars || theme).palette.warning.main, ", transparent 30%) solid 2px"),
        },
        _b["&.".concat(exports.ConfigurableClasses.editMode, ":hover ")] = {
            outline: "color-mix(in srgb, ".concat((theme.vars || theme).palette.warning.main, ", transparent 50%) solid 2px"),
        },
        _b["&.".concat(exports.ConfigurableClasses.editMode, ".").concat(exports.ConfigurableClasses.editorActive, " , &.").concat(exports.ConfigurableClasses.editMode, ".").concat(exports.ConfigurableClasses.editorActive, ":hover ")] = {
            outline: "".concat((theme.vars || theme).palette.warning.main, " solid 2px"),
        },
        _b);
});
//# sourceMappingURL=Configurable.js.map