import { styled } from '@mui/material';
export var DatagridPrefix = 'RaDatagrid';
export var DatagridClasses = {
    root: "".concat(DatagridPrefix, "-root"),
    table: "".concat(DatagridPrefix, "-table"),
    tableWrapper: "".concat(DatagridPrefix, "-tableWrapper"),
    thead: "".concat(DatagridPrefix, "-thead"),
    tbody: "".concat(DatagridPrefix, "-tbody"),
    headerRow: "".concat(DatagridPrefix, "-headerRow"),
    headerCell: "".concat(DatagridPrefix, "-headerCell"),
    checkbox: "".concat(DatagridPrefix, "-checkbox"),
    row: "".concat(DatagridPrefix, "-row"),
    clickableRow: "".concat(DatagridPrefix, "-clickableRow"),
    rowEven: "".concat(DatagridPrefix, "-rowEven"),
    rowOdd: "".concat(DatagridPrefix, "-rowOdd"),
    rowCell: "".concat(DatagridPrefix, "-rowCell"),
    selectable: "".concat(DatagridPrefix, "-selectable"),
    expandHeader: "".concat(DatagridPrefix, "-expandHeader"),
    expandIconCell: "".concat(DatagridPrefix, "-expandIconCell"),
    expandIcon: "".concat(DatagridPrefix, "-expandIcon"),
    expandable: "".concat(DatagridPrefix, "-expandable"),
    expanded: "".concat(DatagridPrefix, "-expanded"),
    expandedPanel: "".concat(DatagridPrefix, "-expandedPanel"),
};
export var DatagridRoot = styled('div', {
    name: DatagridPrefix,
    overridesResolver: function (props, styles) { return styles.root; },
})(function (_a) {
    var _b;
    var theme = _a.theme;
    return (_b = {},
        _b["& .".concat(DatagridClasses.table)] = {
            tableLayout: 'auto',
        },
        _b["& .".concat(DatagridClasses.tableWrapper)] = {},
        _b["& .".concat(DatagridClasses.thead)] = {},
        _b["& .".concat(DatagridClasses.tbody)] = {},
        _b["& .".concat(DatagridClasses.headerRow)] = {},
        _b["& .".concat(DatagridClasses.headerCell)] = {
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
        _b["& .".concat(DatagridClasses.checkbox)] = {},
        _b["& .".concat(DatagridClasses.row)] = {},
        _b["& .".concat(DatagridClasses.clickableRow)] = {
            cursor: 'pointer',
        },
        _b["& .".concat(DatagridClasses.rowEven)] = {},
        _b["& .".concat(DatagridClasses.rowOdd)] = {},
        _b["& .".concat(DatagridClasses.rowCell)] = {},
        _b["& .".concat(DatagridClasses.expandHeader)] = {
            padding: 0,
            width: theme.spacing(6),
        },
        _b["& .".concat(DatagridClasses.expandIconCell)] = {
            width: theme.spacing(6),
        },
        _b["& .".concat(DatagridClasses.expandIcon)] = {
            padding: theme.spacing(1),
            transform: 'rotate(-90deg)',
            transition: theme.transitions.create('transform', {
                duration: theme.transitions.duration.shortest,
            }),
        },
        _b["& .".concat(DatagridClasses.expandIcon, ".").concat(DatagridClasses.expanded)] = {
            transform: 'rotate(0deg)',
        },
        _b["& .".concat(DatagridClasses.expandedPanel)] = {},
        _b);
});
//# sourceMappingURL=useDatagridStyles.js.map