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
exports.FieldToggle = void 0;
var React = __importStar(require("react"));
var ra_core_1 = require("ra-core");
var material_1 = require("@mui/material");
var DragIndicator_1 = __importDefault(require("@mui/icons-material/DragIndicator"));
var styles_1 = require("@mui/material/styles");
/**
 * UI to enable/disable a field
 */
var FieldToggle = function (props) {
    var selected = props.selected, label = props.label, onToggle = props.onToggle, onMove = props.onMove, source = props.source, index = props.index;
    var resource = (0, ra_core_1.useResourceContext)();
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
            React.createElement(material_1.Switch, { checked: selected, onChange: onToggle, name: "".concat(index), id: "switch_".concat(index), size: "small", sx: { mr: 0.5, ml: -0.5 } }),
            React.createElement(material_1.Typography, { variant: "body2", component: "span" },
                React.createElement(ra_core_1.FieldTitle, { label: label, source: source, resource: resource }))),
        onMove && (React.createElement(DragIndicator_1.default, { className: "dragIcon", color: "disabled", fontSize: "small" }))));
};
exports.FieldToggle = FieldToggle;
var PREFIX = 'RaFieldToggle';
var Root = (0, styles_1.styled)('li', {
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