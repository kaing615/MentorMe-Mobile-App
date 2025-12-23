import * as React from 'react';
import { useRef, useEffect, useState, cloneElement, } from 'react';
import { usePreferencesEditor, PreferenceKeyContextProvider, useTranslate, } from 'ra-core';
import { Popover } from '@mui/material';
import { styled, useThemeProps, } from '@mui/material/styles';
import SettingsIcon from '@mui/icons-material/Settings';
import clsx from 'clsx';
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
export var Configurable = function (inProps) {
    var props = useThemeProps({
        props: inProps,
        name: PREFIX,
    });
    var children = props.children, editor = props.editor, preferenceKey = props.preferenceKey, _a = props.openButtonLabel, openButtonLabel = _a === void 0 ? 'ra.configurable.customize' : _a, sx = props.sx;
    var prefixedPreferenceKey = "preferences.".concat(preferenceKey);
    var preferencesEditorContext = usePreferencesEditor();
    var hasPreferencesEditorContext = !!preferencesEditorContext;
    var translate = useTranslate();
    var _b = preferencesEditorContext || {}, isEnabled = _b.isEnabled, setEditor = _b.setEditor, currentPreferenceKey = _b.preferenceKey, setPreferenceKey = _b.setPreferenceKey;
    var isEditorOpen = prefixedPreferenceKey === currentPreferenceKey;
    var editorOpenRef = useRef(isEditorOpen);
    var wrapperRef = useRef(null);
    var _c = useState(false), isCustomizeButtonVisible = _c[0], setIsCustomizeButtonVisible = _c[1];
    useEffect(function () {
        editorOpenRef.current = isEditorOpen;
    }, [isEditorOpen]);
    // on unmount, if selected, remove the editor
    useEffect(function () {
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
        setEditor(cloneElement(editor, {
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
    return (React.createElement(PreferenceKeyContextProvider, { value: prefixedPreferenceKey },
        React.createElement(Root, { className: clsx(ConfigurableClasses.root, isEnabled && ConfigurableClasses.editMode, isEditorOpen && ConfigurableClasses.editorActive), sx: sx, ref: wrapperRef, onMouseEnter: isEnabled ? handleShowButton : undefined, onMouseLeave: isEnabled ? handleHideButton : undefined }, children),
        React.createElement(Popover, { open: !!isEnabled && (isCustomizeButtonVisible || isEditorOpen), sx: {
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
            React.createElement(SettingsIcon
            // @ts-ignore
            , { 
                // @ts-ignore
                fontSize: "12px" }))));
};
var PREFIX = 'RaConfigurable';
export var ConfigurableClasses = {
    root: "".concat(PREFIX, "-root"),
    editMode: "".concat(PREFIX, "-editMode"),
    editorActive: "".concat(PREFIX, "-editorActive"),
};
var Root = styled('span', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {
            position: 'relative',
            display: 'inline-block'
        },
        _b["&.".concat(ConfigurableClasses.editMode)] = {
            transition: theme.transitions.create('outline'),
            outline: "color-mix(in srgb, ".concat((theme.vars || theme).palette.warning.main, ", transparent 30%) solid 2px"),
        },
        _b["&.".concat(ConfigurableClasses.editMode, ":hover ")] = {
            outline: "color-mix(in srgb, ".concat((theme.vars || theme).palette.warning.main, ", transparent 50%) solid 2px"),
        },
        _b["&.".concat(ConfigurableClasses.editMode, ".").concat(ConfigurableClasses.editorActive, " , &.").concat(ConfigurableClasses.editMode, ".").concat(ConfigurableClasses.editorActive, ":hover ")] = {
            outline: "".concat((theme.vars || theme).palette.warning.main, " solid 2px"),
        },
        _b);
});
//# sourceMappingURL=Configurable.js.map