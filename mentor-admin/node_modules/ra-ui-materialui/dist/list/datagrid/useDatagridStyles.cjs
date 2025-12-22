"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.DatagridRoot = exports.DatagridClasses = exports.DatagridPrefix = void 0;
var material_1 = require("@mui/material");
exports.DatagridPrefix = 'RaDatagrid';
exports.DatagridClasses = {
    root: "".concat(exports.DatagridPrefix, "-root"),
    table: "".concat(exports.DatagridPrefix, "-table"),
    tableWrapper: "".concat(exports.DatagridPrefix, "-tableWrapper"),
    thead: "".concat(exports.DatagridPrefix, "-thead"),
    tbody: "".concat(exports.DatagridPrefix, "-tbody"),
    headerRow: "".concat(exports.DatagridPrefix, "-headerRow"),
    headerCell: "".concat(exports.DatagridPrefix, "-headerCell"),
    checkbox: "".concat(exports.DatagridPrefix, "-checkbox"),
    row: "".concat(exports.DatagridPrefix, "-row"),
    clickableRow: "".concat(exports.DatagridPrefix, "-clickableRow"),
    rowEven: "".concat(exports.DatagridPrefix, "-rowEven"),
    rowOdd: "".concat(exports.DatagridPrefix, "-rowOdd"),
    rowCell: "".concat(exports.DatagridPrefix, "-rowCell"),
    selectable: "".concat(exports.DatagridPrefix, "-selectable"),
    expandHeader: "".concat(exports.DatagridPrefix, "-expandHeader"),
    expandIconCell: "".concat(exports.DatagridPrefix, "-expandIconCell"),
    expandIcon: "".concat(exports.DatagridPrefix, "-expandIcon"),
    expandable: "".concat(exports.DatagridPrefix, "-expandable"),
    expanded: "".concat(exports.DatagridPrefix, "-expanded"),
    expandedPanel: "".concat(exports.DatagridPrefix, "-expandedPanel"),
};
exports.DatagridRoot = (0, material_1.styled)('div', {
    name: exports.DatagridPrefix,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["& .".concat(exports.DatagridClasses.table)] = {
            tableLayout: 'auto',
        },
        _b["& .".concat(exports.DatagridClasses.tableWrapper)] = {},
        _b["& .".concat(exports.DatagridClasses.thead)] = {},
        _b["& .".concat(exports.DatagridClasses.tbody)] = {},
        _b["& .".concat(exports.DatagridClasses.headerRow)] = {},
        _b["& .".concat(exports.DatagridClasses.headerCell)] = {
            position: 'sticky',
            top: 0,
            zIndex: 2,
            backgroundColor: (theme.vars || theme).palette.background.paper,
            '&:first-of-type': {
                borderTopLeftRadius: theme.shape.borderRadius,
            },
            '&:last-child': {
                borderTopRightRadius: theme.shape.borderRadius,
            },
        },
        _b["& .".concat(exports.DatagridClasses.checkbox)] = {},
        _b["& .".concat(exports.DatagridClasses.row)] = {},
        _b["& .".concat(exports.DatagridClasses.clickableRow)] = {
            cursor: 'pointer',
        },
        _b["& .".concat(exports.DatagridClasses.rowEven)] = {},
        _b["& .".concat(exports.DatagridClasses.rowOdd)] = {},
        _b["& .".concat(exports.DatagridClasses.rowCell)] = {},
        _b["& .".concat(exports.DatagridClasses.expandHeader)] = {
            padding: 0,
            width: theme.spacing(6),
        },
        _b["& .".concat(exports.DatagridClasses.expandIconCell)] = {
            width: theme.spacing(6),
        },
        _b["& .".concat(exports.DatagridClasses.expandIcon)] = {
            padding: theme.spacing(1),
            transform: 'rotate(-90deg)',
            transition: theme.transitions.create('transform', {
                duration: theme.transitions.duration.shortest,
            }),
        },
        _b["& .".concat(exports.DatagridClasses.expandIcon, ".").concat(exports.DatagridClasses.expanded)] = {
            transform: 'rotate(0deg)',
        },
        _b["& .".concat(exports.DatagridClasses.expandedPanel)] = {},
        _b);
});
//# sourceMappingURL=useDatagridStyles.js.map