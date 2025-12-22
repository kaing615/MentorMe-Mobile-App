import * as React from 'react';
import { FieldTitle, useResourceContext } from 'ra-core';
import { Switch, Typography } from '@mui/material';
import DragIcon from '@mui/icons-material/DragIndicator';
import { styled } from '@mui/material/styles';
/**
 * UI to enable/disable a field
 */
export var FieldToggle = function (props) {
    var selected = props.selected, label = props.label, onToggle = props.onToggle, onMove = props.onMove, source = props.source, index = props.index;
    var resource = useResourceContext();
    var dropIndex = React.useRef(null);
    var x = React.useRef(null);
    var y = React.useRef(null);
    var handleDocumentDragOver = React.useCallback(function (event) {
        x.current = event.clientX;
        y.current = event.clientY;
    }, []);
    var handleDragStart = function () {
        document.addEventListener('dragover', handleDocumentDragOver);
    };
    var handleDrag = function (event) {
        // imperative DOM manipulations using the native Drag API
        var selectedItem = event.target;
        selectedItem.classList.add('drag-active');
        var list = selectedItem.closest('ul');
        if (x.current == null || y.current == null) {
            return;
        }
        var elementAtDragCoordinates = document.elementFromPoint(x.current, y.current);
        var dropItem = elementAtDragCoordinates === null
            ? selectedItem
            : elementAtDragCoordinates.closest('li');
        if (!dropItem) {
            return;
        }
        if (dropItem.classList.contains('dragIcon')) {
            dropItem = dropItem.parentNode;
        }
        if (dropItem === selectedItem) {
            return;
        }
        if (list === dropItem.parentNode.closest('ul')) {
            dropIndex.current = dropItem.dataset.index;
            if (dropItem === selectedItem.nextSibling) {
                dropItem = dropItem.nextSibling;
            }
            list.insertBefore(selectedItem, dropItem);
        }
    };
    var handleDragEnd = function (event) {
        var selectedItem = event.target;
        var list = selectedItem.closest('ul');
        var elementFromPoint = x.current != null && y.current != null
            ? document.elementFromPoint(x.current, y.current)
            : null;
        var dropItem = x.current == null || y.current == null || elementFromPoint === null
            ? selectedItem
            : elementFromPoint.closest('li');
        if (y.current !== null && list && !dropItem) {
            var closestUL = selectedItem.closest('ul');
            if (closestUL &&
                y.current > closestUL.getBoundingClientRect().bottom) {
                dropItem = list.lastChild;
            }
            else {
                dropItem = list.firstChild;
            }
        }
        if (dropItem && list === dropItem.closest('ul')) {
            if (onMove)
                onMove(selectedItem.dataset.index, dropIndex.current);
        }
        else {
            event.preventDefault();
            event.stopPropagation();
        }
        selectedItem.classList.remove('drag-active');
        document.removeEventListener('dragover', handleDocumentDragOver);
    };
    var handleDragOver = function (event) {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
    };
    return (React.createElement(Root, { key: source, role: "option", draggable: onMove ? 'true' : undefined, onDrag: onMove ? handleDrag : undefined, onDragStart: onMove ? handleDragStart : undefined, onDragEnd: onMove ? handleDragEnd : undefined, onDragOver: onMove ? handleDragOver : undefined, "data-index": index },
        React.createElement("label", { htmlFor: "switch_".concat(index) },
            React.createElement(Switch, { checked: selected, onChange: onToggle, name: "".concat(index), id: "switch_".concat(index), size: "small", sx: { mr: 0.5, ml: -0.5 } }),
            React.createElement(Typography, { variant: "body2", component: "span" },
                React.createElement(FieldTitle, { label: label, source: source, resource: resource }))),
        onMove && (React.createElement(DragIcon, { className: "dragIcon", color: "disabled", fontSize: "small" }))));
};
var PREFIX = 'RaFieldToggle';
var Root = styled('li', {
    name: PREFIX,
    overridesResolver: function (_props, styles) { return styles.root; },
})(function (_a) {
    var theme = _a.theme;
    return ({
        display: 'flex',
        justifyContent: 'space-between',
        paddingLeft: 0,
        '& svg': {
            cursor: 'move',
        },
        '&.drag-active': {
            background: 'transparent',
            color: 'transparent',
            outline: "1px solid ".concat((theme.vars || theme).palette.action.selected),
            '& .MuiSwitch-root, & svg': {
                visibility: 'hidden',
            },
        },
    });
});
//# sourceMappingURL=FieldToggle.js.map