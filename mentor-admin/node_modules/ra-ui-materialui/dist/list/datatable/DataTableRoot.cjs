"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.DataTableRoot = exports.DataTableClasses = void 0;
var material_1 = require("@mui/material");
var PREFIX = 'RaDataTable';
exports.DataTableClasses = {
    root: "".concat(PREFIX, "-root"),
    table: "".concat(PREFIX, "-table"),
    tableWrapper: "".concat(PREFIX, "-tableWrapper"),
    thead: "".concat(PREFIX, "-thead"),
    tbody: "".concat(PREFIX, "-tbody"),
    headerRow: "".concat(PREFIX, "-headerRow"),
    headerCell: "".concat(PREFIX, "-headerCell"),
    checkbox: "".concat(PREFIX, "-checkbox"),
    row: "".concat(PREFIX, "-row"),
    clickableRow: "".concat(PREFIX, "-clickableRow"),
    rowEven: "".concat(PREFIX, "-rowEven"),
    rowOdd: "".concat(PREFIX, "-rowOdd"),
    rowCell: "".concat(PREFIX, "-rowCell"),
    selectable: "".concat(PREFIX, "-selectable"),
    expandHeader: "".concat(PREFIX, "-expandHeader"),
    expandIconCell: "".concat(PREFIX, "-expandIconCell"),
    expandIcon: "".concat(PREFIX, "-expandIcon"),
    expandable: "".concat(PREFIX, "-expandable"),
    expanded: "".concat(PREFIX, "-expanded"),
    expandRow: "".concat(PREFIX, "-expandRow"),
};
exports.DataTableRoot = (0, material_1.styled)('div', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["& .".concat(exports.DataTableClasses.table)] = {
            tableLayout: 'auto',
        },
        _b["& .".concat(exports.DataTableClasses.tableWrapper)] = {},
        _b["& .".concat(exports.DataTableClasses.thead)] = {},
        _b["& .".concat(exports.DataTableClasses.tbody)] = {},
        _b["& .".concat(exports.DataTableClasses.headerRow)] = {},
        _b["& .".concat(exports.DataTableClasses.headerCell)] = {
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
        _b["& .".concat(exports.DataTableClasses.checkbox)] = {},
        _b["& .".concat(exports.DataTableClasses.row)] = {},
        _b["& .".concat(exports.DataTableClasses.clickableRow)] = {
            cursor: 'pointer',
        },
        _b["& .".concat(exports.DataTableClasses.rowEven)] = {},
        _b["& .".concat(exports.DataTableClasses.rowOdd)] = {},
        _b["& .".concat(exports.DataTableClasses.rowCell)] = {},
        _b["& .".concat(exports.DataTableClasses.expandable, " > td")] = {
            borderBottom: 'unset',
        },
        _b["& .".concat(exports.DataTableClasses.expandHeader)] = {
            paddingRight: 0,
            width: theme.spacing(4),
        },
        _b["& .".concat(exports.DataTableClasses.expandIconCell)] = {
            paddingRight: 0,
            width: theme.spacing(4),
        },
        _b["& .".concat(exports.DataTableClasses.expandIcon)] = {
            padding: theme.spacing(1),
            transform: 'rotate(-90deg)',
            transition: theme.transitions.create('transform', {
                duration: theme.transitions.duration.short,
            }),
        },
        _b["& .".concat(exports.DataTableClasses.expandIcon, ".").concat(exports.DataTableClasses.expanded)] = {
            transform: 'rotate(0deg)',
        },
        _b["& .".concat(exports.DataTableClasses.expandRow)] = {},
        _b);
});
//# sourceMappingURL=DataTableRoot.js.map