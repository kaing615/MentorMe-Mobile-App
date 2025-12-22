import { styled } from '@mui/material';
var PREFIX = 'RaDataTable';
export var DataTableClasses = {
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
export var DataTableRoot = styled('div', {
    name: PREFIX,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["& .".concat(DataTableClasses.table)] = {
            tableLayout: 'auto',
        },
        _b["& .".concat(DataTableClasses.tableWrapper)] = {},
        _b["& .".concat(DataTableClasses.thead)] = {},
        _b["& .".concat(DataTableClasses.tbody)] = {},
        _b["& .".concat(DataTableClasses.headerRow)] = {},
        _b["& .".concat(DataTableClasses.headerCell)] = {
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
        _b["& .".concat(DataTableClasses.checkbox)] = {},
        _b["& .".concat(DataTableClasses.row)] = {},
        _b["& .".concat(DataTableClasses.clickableRow)] = {
            cursor: 'pointer',
        },
        _b["& .".concat(DataTableClasses.rowEven)] = {},
        _b["& .".concat(DataTableClasses.rowOdd)] = {},
        _b["& .".concat(DataTableClasses.rowCell)] = {},
        _b["& .".concat(DataTableClasses.expandable, " > td")] = {
            borderBottom: 'unset',
        },
        _b["& .".concat(DataTableClasses.expandHeader)] = {
            paddingRight: 0,
            width: theme.spacing(4),
        },
        _b["& .".concat(DataTableClasses.expandIconCell)] = {
            paddingRight: 0,
            width: theme.spacing(4),
        },
        _b["& .".concat(DataTableClasses.expandIcon)] = {
            padding: theme.spacing(1),
            transform: 'rotate(-90deg)',
            transition: theme.transitions.create('transform', {
                duration: theme.transitions.duration.short,
            }),
        },
        _b["& .".concat(DataTableClasses.expandIcon, ".").concat(DataTableClasses.expanded)] = {
            transform: 'rotate(0deg)',
        },
        _b["& .".concat(DataTableClasses.expandRow)] = {},
        _b);
});
//# sourceMappingURL=DataTableRoot.js.map