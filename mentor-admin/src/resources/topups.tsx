import * as React from "react";
import {
  Box,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
  Button,
  Chip,
} from "@mui/material";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import CancelIcon from "@mui/icons-material/Cancel";
import QrCodeIcon from "@mui/icons-material/QrCode2";
import { useNotify } from "react-admin";

const apiUrl = import.meta.env.VITE_API_URL;

/* ================= TYPES ================= */

type TopupStatus = "PENDING" | "APPROVED" | "REJECTED";

type TopUpItem = {
  id: string;
  amount: number;
  currency?: string;
  note?: string;
  status?: TopupStatus;
  qrImageUrl?: string | null;
  referenceCode?: string;
  createdAt?: string;
  user?: {
    userName?: string;
    email?: string;
  };
};

/* ================= UI HELPERS ================= */

const StatusChip = ({ status }: { status?: TopupStatus }) => {
  const map: Record<TopupStatus, "warning" | "success" | "error"> = {
    PENDING: "warning",
    APPROVED: "success",
    REJECTED: "error",
  };

  if (!status) {
    return <Chip size="small" label="UNKNOWN" variant="outlined" />;
  }

  return (
    <Chip size="small" label={status} color={map[status]} variant="outlined" />
  );
};

/* ================= MAIN ================= */

export function TopUpList() {
  const notify = useNotify();
  const token = localStorage.getItem("access_token") || "";

  const [loading, setLoading] = React.useState(false);
  const [items, setItems] = React.useState<TopUpItem[]>([]);

  const [approveId, setApproveId] = React.useState<string | null>(null);
  const [rejectId, setRejectId] = React.useState<string | null>(null);
  const [rejectReason, setRejectReason] = React.useState("");

  /* ---------------- FETCH ---------------- */

  const fetchList = React.useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(`${apiUrl}/wallet/admin/topups/pending`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      const json = await res.json();
      const list = json?.data?.items ?? [];

      setItems(
        list.map((it: any) => ({
          id: it.id ?? it._id,
          ...it,
        })),
      );
    } catch {
      notify("Không tải được danh sách topup", { type: "error" });
    } finally {
      setLoading(false);
    }
  }, [token, notify]);

  React.useEffect(() => {
    fetchList();
  }, [fetchList]);

  /* ---------------- ACTIONS ---------------- */

  const approveTopup = async () => {
    if (!approveId) return;
    await fetch(`${apiUrl}/wallet/admin/topups/${approveId}/approve`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
    });
    notify("Topup đã được duyệt", { type: "success" });
    setApproveId(null);
    fetchList();
  };

  const rejectTopup = async () => {
    if (!rejectId || !rejectReason.trim()) {
      notify("Nhập lý do từ chối", { type: "warning" });
      return;
    }
    await fetch(`${apiUrl}/wallet/admin/topups/${rejectId}/reject`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ reason: rejectReason }),
    });
    notify("Topup đã bị từ chối", { type: "info" });
    setRejectId(null);
    setRejectReason("");
    fetchList();
  };

  /* ================= UI ================= */

  return (
    <Box p={3}>
      <Typography variant="h4" fontWeight={600} gutterBottom>
        Top Up Intents
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography color="text.secondary">
            Các yêu cầu nạp tiền đang chờ admin xử lý.
          </Typography>
        </CardContent>
      </Card>

      {loading ? (
        <Box display="flex" justifyContent="center" mt={6}>
          <CircularProgress />
        </Box>
      ) : (
        <Table sx={{ borderRadius: 2 }}>
          <TableHead>
            <TableRow>
              <TableCell>User</TableCell>
              <TableCell>Reference</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Created</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {items.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  Không có yêu cầu nào
                </TableCell>
              </TableRow>
            )}

            {items.map((it) => (
              <TableRow key={it.id} hover>
                <TableCell>
                  <Typography fontWeight={500}>
                    {it.user?.userName ?? it.user?.email ?? "—"}
                  </Typography>
                </TableCell>

                <TableCell>{it.referenceCode ?? "—"}</TableCell>

                <TableCell>
                  <Typography fontWeight={600}>
                    {it.amount.toLocaleString()} {it.currency ?? "VND"}
                  </Typography>
                </TableCell>

                <TableCell>
                  <StatusChip status={it.status} />
                </TableCell>

                <TableCell>
                  {it.createdAt ? new Date(it.createdAt).toLocaleString() : "—"}
                </TableCell>

                <TableCell align="right">
                  <Stack direction="row" spacing={1} justifyContent="flex-end">
                    {it.qrImageUrl && (
                      <Tooltip title="View QR">
                        <IconButton>
                          <QrCodeIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}

                    <Tooltip title="Approve">
                      <IconButton
                        color="success"
                        onClick={() => setApproveId(it.id)}
                      >
                        <CheckCircleIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>

                    <Tooltip title="Reject">
                      <IconButton
                        color="error"
                        onClick={() => setRejectId(it.id)}
                      >
                        <CancelIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      {/* APPROVE */}
      <Dialog open={!!approveId} onClose={() => setApproveId(null)}>
        <DialogTitle>Duyệt Topup</DialogTitle>
        <DialogContent>
          <Typography>Xác nhận duyệt topup này?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setApproveId(null)}>Huỷ</Button>
          <Button variant="contained" onClick={approveTopup}>
            Duyệt
          </Button>
        </DialogActions>
      </Dialog>

      {/* REJECT */}
      <Dialog open={!!rejectId} onClose={() => setRejectId(null)}>
        <DialogTitle>Từ chối Topup</DialogTitle>
        <DialogContent>
          <TextField
            label="Lý do"
            fullWidth
            multiline
            minRows={3}
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectId(null)}>Huỷ</Button>
          <Button color="error" variant="contained" onClick={rejectTopup}>
            Từ chối
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
