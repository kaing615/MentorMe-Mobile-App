import * as React from "react";
import {
  List,
  Datagrid,
  TextField,
  NumberField,
  DateField,
  TextInput,
  SelectInput,
  useNotify,
  useRefresh,
  useRecordContext,
  Confirm,
} from "react-admin";
import {
  Chip,
  Stack,
  IconButton,
  Tooltip,
  Typography,
  Drawer,
  Box,
  Divider,
} from "@mui/material";

import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import ReplayIcon from "@mui/icons-material/Replay";
import CancelIcon from "@mui/icons-material/Cancel";
import VisibilityIcon from "@mui/icons-material/Visibility";

const apiUrl = import.meta.env.VITE_API_URL;

/* ================= TYPES ================= */

type Payout = {
  id: string;
  mentorId: string;
  amount: number;
  currency: string;
  status: "PENDING" | "PROCESSING" | "PAID" | "FAILED";
  attemptCount: number;
  externalId?: string;
  createdAt: string;
  updatedAt: string;
};

/* ================= FILTERS ================= */

const statusChoices = [
  { id: "PENDING", name: "Pending" },
  { id: "PROCESSING", name: "Processing" },
  { id: "PAID", name: "Paid" },
  { id: "FAILED", name: "Failed" },
];

/* ================= UI FIELDS ================= */

function ShortId() {
  const record = useRecordContext<Payout>();
  if (!record) return null;
  return (
    <Typography fontSize={13} color="text.secondary">
      #{record.id.slice(-6)}
    </Typography>
  );
}

function AmountField() {
  const record = useRecordContext<Payout>();
  if (!record) return null;
  return (
    <Typography fontWeight={600}>
      {record.amount.toLocaleString()} {record.currency}
    </Typography>
  );
}

function StatusChip() {
  const record = useRecordContext<Payout>();
  if (!record) return null;

  const map: any = {
    PENDING: "warning",
    PROCESSING: "info",
    PAID: "success",
    FAILED: "error",
  };

  return (
    <Chip
      size="small"
      label={record.status}
      color={map[record.status]}
      variant="outlined"
    />
  );
}

/* ================= DETAIL DRAWER ================= */

function PayoutDrawer({
  open,
  onClose,
  record,
}: {
  open: boolean;
  onClose: () => void;
  record: Payout | null;
}) {
  if (!record) return null;

  return (
    <Drawer anchor="right" open={open} onClose={onClose}>
      <Box sx={{ width: 360, p: 2 }}>
        <Typography variant="h6">Payout Detail</Typography>
        <Divider sx={{ my: 2 }} />

        <Typography>ID: {record.id}</Typography>
        <Typography>Mentor: {record.mentorId}</Typography>
        <Typography>
          Amount: {record.amount.toLocaleString()} {record.currency}
        </Typography>
        <Typography>Status: {record.status}</Typography>
        <Typography>Attempts: {record.attemptCount}</Typography>
        <Typography>External ID: {record.externalId || "—"}</Typography>
        <Typography>
          Created: {new Date(record.createdAt).toLocaleString()}
        </Typography>
        <Typography>
          Updated: {new Date(record.updatedAt).toLocaleString()}
        </Typography>
      </Box>
    </Drawer>
  );
}

/* ================= ACTIONS ================= */

function PayoutActions() {
  const record = useRecordContext<Payout>();
  const notify = useNotify();
  const refresh = useRefresh();

  const [confirm, setConfirm] = React.useState<
    "approve" | "retry" | "reject" | null
  >(null);
  const [drawerOpen, setDrawerOpen] = React.useState(false);

  if (!record) return null;

  const callApi = async (path: string, message: string, type: any) => {
    await fetch(`${apiUrl}${path}`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("access_token")}`,
      },
    });
    notify(message, { type });
    refresh();
    setConfirm(null);
  };

  return (
    <>
      <Stack direction="row" spacing={1}>
        <Tooltip title="View detail">
          <IconButton onClick={() => setDrawerOpen(true)}>
            <VisibilityIcon fontSize="small" />
          </IconButton>
        </Tooltip>

        {record.status === "PENDING" && (
          <Tooltip title="Approve payout">
            <IconButton color="success" onClick={() => setConfirm("approve")}>
              <CheckCircleIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}

        {["FAILED", "PROCESSING"].includes(record.status) && (
          <Tooltip title="Retry payout">
            <IconButton color="warning" onClick={() => setConfirm("retry")}>
              <ReplayIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}

        {record.status === "PENDING" && (
          <Tooltip title="Reject payout">
            <IconButton color="error" onClick={() => setConfirm("reject")}>
              <CancelIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
      </Stack>

      {/* CONFIRMS */}
      <Confirm
        isOpen={confirm === "approve"}
        title="Approve payout"
        content="Approve this payout request?"
        confirmColor="primary"
        onConfirm={() =>
          callApi(
            `/admin/payouts/${record.id}/approve`,
            "Payout approved",
            "success",
          )
        }
        onClose={() => setConfirm(null)}
      />

      <Confirm
        isOpen={confirm === "retry"}
        title="Retry payout"
        content="Retry this payout?"
        confirmColor="warning"
        onConfirm={() =>
          callApi(
            `/admin/payouts/${record.id}/retry`,
            "Retry triggered",
            "info",
          )
        }
        onClose={() => setConfirm(null)}
      />

      <Confirm
        isOpen={confirm === "reject"}
        title="Reject payout"
        content="Reject this payout request?"
        confirmColor="warning"
        onConfirm={() =>
          callApi(
            `/admin/payouts/${record.id}/reject`,
            "Payout rejected",
            "info",
          )
        }
        onClose={() => setConfirm(null)}
      />

      <PayoutDrawer
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        record={record}
      />
    </>
  );
}

/* ================= LIST ================= */

export function PayoutList() {
  return (
    <List
      title="💸 Mentor Payouts"
      filters={[
        <TextInput key="mentorId" source="mentorId" label="Mentor ID" />,
        <SelectInput
          key="status"
          source="status"
          choices={statusChoices}
          alwaysOn
        />,
      ]}
      sort={{ field: "createdAt", order: "DESC" }}
      perPage={25}
    >
      <Datagrid
        rowClick={false}
        sx={{
          "& .MuiTableCell-root": { fontSize: 14 },
          "& .RaDatagrid-row": { height: 56 },
        }}
      >
        <ShortId />
        <TextField source="mentorId" />
        <AmountField />
        <StatusChip />
        <NumberField source="attemptCount" />
        <TextField source="externalId" />
        <DateField source="createdAt" showTime />
        <PayoutActions />
      </Datagrid>
    </List>
  );
}
