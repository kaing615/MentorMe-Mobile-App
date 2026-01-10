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
  { id: "PENDING", name: "Chờ duyệt" },
  { id: "PROCESSING", name: "Đang xử lý" },
  { id: "PAID", name: "Đã chi trả" },
  { id: "FAILED", name: "Thất bại" },
];

const statusLabelMap = statusChoices.reduce<Record<string, string>>((acc, choice) => {
  acc[choice.id] = choice.name;
  return acc;
}, {});

/* ================= UI FIELDS ================= */

function ShortId({ label: _label }: { label?: string }) {
  const record = useRecordContext<Payout>();
  if (!record) return null;
  return (
    <Typography fontSize={13} color="text.secondary">
      #{record.id.slice(-6)}
    </Typography>
  );
}

function AmountField({ label: _label }: { label?: string }) {
  const record = useRecordContext<Payout>();
  if (!record) return null;
  return (
    <Typography fontWeight={600}>
      {record.amount.toLocaleString()} {record.currency}
    </Typography>
  );
}

function StatusChip({ label: _label }: { label?: string }) {
  const record = useRecordContext<Payout>();
  if (!record) return null;

  const map: Record<string, "warning" | "info" | "success" | "error"> = {
    PENDING: "warning",
    PROCESSING: "info",
    PAID: "success",
    FAILED: "error",
  };

  return (
    <Chip
      size="small"
      label={statusLabelMap[record.status] || record.status}
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
        <Typography variant="h6">Chi tiết yêu cầu rút tiền</Typography>
        <Divider sx={{ my: 2 }} />

        <Typography>Mã: {record.id}</Typography>
        <Typography>Mentor: {record.mentorId}</Typography>
        <Typography>
          Số tiền: {record.amount.toLocaleString()} {record.currency}
        </Typography>
        <Typography>
          Trạng thái: {statusLabelMap[record.status] || record.status}
        </Typography>
        <Typography>Số lần thử: {record.attemptCount}</Typography>
        <Typography>Mã ngoài: {record.externalId || "—"}</Typography>
        <Typography>
          Tạo lúc: {new Date(record.createdAt).toLocaleString()}
        </Typography>
        <Typography>
          Cập nhật: {new Date(record.updatedAt).toLocaleString()}
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

  const callApi = async (path: string, message: string, type: "success" | "info") => {
    try {
      const res = await fetch(`${apiUrl}${path}`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("access_token")}`,
        },
      });

      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Thao tác thất bại");
      }

      notify(message, { type });
      refresh();
      setConfirm(null);
    } catch (e: any) {
      notify(e.message || "Thao tác thất bại", { type: "error" });
      setConfirm(null);
    }
  };

  return (
    <>
      <Stack direction="row" spacing={1}>
        <Tooltip title="Xem chi tiết">
          <IconButton onClick={() => setDrawerOpen(true)}>
            <VisibilityIcon fontSize="small" />
          </IconButton>
        </Tooltip>

        {record.status === "PENDING" && (
          <Tooltip title="Duyệt yêu cầu">
            <IconButton color="success" onClick={() => setConfirm("approve")}>
              <CheckCircleIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}

        {["FAILED", "PROCESSING"].includes(record.status) && (
          <Tooltip title="Thử lại">
            <IconButton color="warning" onClick={() => setConfirm("retry")}>
              <ReplayIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}

        {record.status === "PENDING" && (
          <Tooltip title="Từ chối yêu cầu">
            <IconButton color="error" onClick={() => setConfirm("reject")}>
              <CancelIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
      </Stack>

      <Confirm
        isOpen={confirm === "approve"}
        title="Duyệt yêu cầu rút tiền"
        content="Duyệt yêu cầu rút tiền này?"
        confirmColor="primary"
        onConfirm={() =>
          callApi(`/admin/payouts/${record.id}/approve`, "Đã duyệt yêu cầu", "success")
        }
        onClose={() => setConfirm(null)}
      />

      <Confirm
        isOpen={confirm === "retry"}
        title="Thử lại yêu cầu"
        content="Thử lại yêu cầu rút tiền này?"
        confirmColor="warning"
        onConfirm={() =>
          callApi(`/admin/payouts/${record.id}/retry`, "Đã gửi yêu cầu thử lại", "info")
        }
        onClose={() => setConfirm(null)}
      />

      <Confirm
        isOpen={confirm === "reject"}
        title="Từ chối yêu cầu"
        content="Từ chối yêu cầu rút tiền này?"
        confirmColor="warning"
        onConfirm={() =>
          callApi(`/admin/payouts/${record.id}/reject`, "Đã từ chối yêu cầu", "info")
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
      title="Yêu cầu rút tiền"
      filters={[
        <TextInput key="mentorId" source="mentorId" label="ID mentor" />,
        <SelectInput
          key="status"
          source="status"
          choices={statusChoices}
          alwaysOn
          label="Trạng thái"
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
        <ShortId label="Mã yêu cầu" />
        <TextField source="mentorId" label="ID mentor" />
        <AmountField label="Số tiền" />
        <StatusChip label="Trạng thái" />
        <NumberField source="attemptCount" label="Số lần thử" />
        <TextField source="externalId" label="Mã ngoài" />
        <DateField source="createdAt" showTime label="Tạo lúc" />
        <PayoutActions />
      </Datagrid>
    </List>
  );
}
