import CancelIcon from "@mui/icons-material/Cancel";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import QrCodeIcon from "@mui/icons-material/QrCode2";
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
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
  ToggleButton,
  ToggleButtonGroup,
  Tooltip,
  Typography,
} from "@mui/material";
import * as React from "react";
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
  reviewedAt?: string;
  user?: {
    userName?: string;
    email?: string;
  };
  reviewedBy?: {
    userName?: string;
    email?: string;
  };
};

/* ================= UI HELPERS ================= */

const statusLabelMap: Record<TopupStatus, string> = {
  PENDING: "Chờ duyệt",
  APPROVED: "Đã duyệt",
  REJECTED: "Từ chối",
};

const StatusChip = ({ status }: { status?: TopupStatus }) => {
  const map: Record<TopupStatus, "warning" | "success" | "error"> = {
    PENDING: "warning",
    APPROVED: "success",
    REJECTED: "error",
  };

  if (!status) {
    return <Chip size="small" label="Không rõ" variant="outlined" />;
  }

  return (
    <Chip
      size="small"
      label={statusLabelMap[status] || status}
      color={map[status]}
      variant="outlined"
    />
  );
};

/* ================= MAIN ================= */

export function TopUpList() {
  const notify = useNotify();
  const token = localStorage.getItem("access_token") || "";

  const [loading, setLoading] = React.useState(false);
  const [items, setItems] = React.useState<TopUpItem[]>([]);
  const [statusFilter, setStatusFilter] = React.useState<string>("ALL");

  const [approveId, setApproveId] = React.useState<string | null>(null);
  const [rejectId, setRejectId] = React.useState<string | null>(null);
  const [rejectReason, setRejectReason] = React.useState("");
  const [qrPreview, setQrPreview] = React.useState<{
    url: string;
    reference?: string;
  } | null>(null);

  /* ---------------- FETCH ---------------- */

  const fetchList = React.useCallback(async () => {
    setLoading(true);
    try {
      // Use the new /admin/topups endpoint with status filter
      const url = statusFilter === "ALL" || !statusFilter
        ? `${apiUrl}/wallet/admin/topups`
        : `${apiUrl}/wallet/admin/topups?status=${statusFilter}`;

      const res = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({ message: 'Unknown error' }));
        console.error('Failed to fetch topups:', res.status, errorData);
        notify(`Không tải được danh sách topup: ${errorData.message || res.statusText}`, { type: "error" });
        return;
      }

      const json = await res.json();
      const list = json?.data?.items ?? [];

      setItems(
        list.map((it: any) => ({
          id: it.id ?? it._id,
          ...it,
        }))
      );
    } catch (error) {
      console.error('Error fetching topups:', error);
      notify("Không tải được danh sách topup", { type: "error" });
    } finally {
      setLoading(false);
    }
  }, [token, notify, statusFilter]);

  React.useEffect(() => {
    fetchList();
  }, [fetchList]);

  /* ---------------- ACTIONS ---------------- */

  const approveTopup = async () => {
    if (!approveId) return;
    try {
      const res = await fetch(`${apiUrl}/wallet/admin/topups/${approveId}/approve`, {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Duyệt thất bại");
      }
      notify("Topup đã được duyệt", { type: "success" });
      setApproveId(null);
      fetchList();
    } catch (e: any) {
      notify(e.message || "Duyệt thất bại", { type: "error" });
    }
  };

  const rejectTopup = async () => {
    if (!rejectId || !rejectReason.trim()) {
      notify("Nhập lý do từ chối", { type: "warning" });
      return;
    }
    try {
      const res = await fetch(`${apiUrl}/wallet/admin/topups/${rejectId}/reject`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ reason: rejectReason }),
      });
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Từ chối thất bại");
      }
      notify("Topup đã bị từ chối", { type: "info" });
      setRejectId(null);
      setRejectReason("");
      fetchList();
    } catch (e: any) {
      notify(e.message || "Từ chối thất bại", { type: "error" });
    }
  };

  /* ================= UI ================= */

  return (
    <Box p={3}>
      <Typography variant="h4" fontWeight={600} gutterBottom>
        Yêu cầu nạp tiền
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack spacing={2}>
            <Typography color="text.secondary">
              Các yêu cầu nạp tiền từ người dùng.
            </Typography>
            
            <Box>
              <Typography variant="subtitle2" gutterBottom>
                Lọc theo trạng thái:
              </Typography>
              <ToggleButtonGroup
                value={statusFilter}
                exclusive
                onChange={(_, newValue) => {
                  if (newValue !== null) setStatusFilter(newValue);
                }}
                size="small"
              >
                <ToggleButton value="ALL">Tất cả</ToggleButton>
                <ToggleButton value="PENDING">Chờ duyệt</ToggleButton>
                <ToggleButton value="SUBMITTED">Đã chuyển</ToggleButton>
                <ToggleButton value="APPROVED">Đã duyệt</ToggleButton>
                <ToggleButton value="REJECTED">Từ chối</ToggleButton>
              </ToggleButtonGroup>
            </Box>
          </Stack>
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
              <TableCell>Người dùng</TableCell>
              <TableCell>Mã tham chiếu</TableCell>
              <TableCell>Số tiền</TableCell>
              <TableCell>Trạng thái</TableCell>
              <TableCell>Tạo lúc</TableCell>
              <TableCell>Người xử lý</TableCell>
              <TableCell align="right">Hành động</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {items.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center">
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

                <TableCell>
                  {it.reviewedBy ? (
                    <Box>
                      <Typography variant="body2" fontWeight={500}>
                        {it.reviewedBy.userName ?? it.reviewedBy.email ?? "—"}
                      </Typography>
                      {it.reviewedAt && (
                        <Typography variant="caption" color="text.secondary">
                          {new Date(it.reviewedAt).toLocaleString()}
                        </Typography>
                      )}
                    </Box>
                  ) : (
                    "—"
                  )}
                </TableCell>

                <TableCell align="right">
                  <Stack direction="row" spacing={1} justifyContent="flex-end">
                    {it.qrImageUrl && (
                      <Tooltip title="Xem QR">
                        <IconButton
                          onClick={() =>
                            setQrPreview({
                              url: it.qrImageUrl as string,
                              reference: it.referenceCode,
                            })
                          }
                        >
                          <QrCodeIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}

                    {(it.status === "PENDING" || it.status === "SUBMITTED") && (
                      <>
                        <Tooltip title="Duyệt">
                          <IconButton
                            color="success"
                            onClick={() => setApproveId(it.id)}
                          >
                            <CheckCircleIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        <Tooltip title="Từ chối">
                          <IconButton
                            color="error"
                            onClick={() => setRejectId(it.id)}
                          >
                            <CancelIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </>
                    )}

                    {it.status === "APPROVED" && (
                      <Chip size="small" label="Đã xử lý" color="success" />
                    )}
                    
                    {it.status === "REJECTED" && (
                      <Chip size="small" label="Đã từ chối" color="error" />
                    )}
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <Dialog open={!!approveId} onClose={() => setApproveId(null)}>
        <DialogTitle>Duyệt yêu cầu nạp tiền</DialogTitle>
        <DialogContent>
          <Typography>Xác nhận duyệt yêu cầu này?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setApproveId(null)}>Hủy</Button>
          <Button variant="contained" onClick={approveTopup}>
            Duyệt
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!rejectId} onClose={() => setRejectId(null)}>
        <DialogTitle>Từ chối yêu cầu nạp tiền</DialogTitle>
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
          <Button onClick={() => setRejectId(null)}>Hủy</Button>
          <Button color="error" variant="contained" onClick={rejectTopup}>
            Từ chối
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={!!qrPreview} onClose={() => setQrPreview(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Mã QR nạp tiền</DialogTitle>
        <DialogContent>
          <Stack spacing={2} alignItems="center">
            {qrPreview?.reference && (
              <Typography color="text.secondary">
                Mã tham chiếu: {qrPreview.reference}
              </Typography>
            )}
            {qrPreview?.url && (
              <Box
                component="img"
                src={qrPreview.url}
                alt="QR nạp tiền"
                sx={{ maxWidth: "100%", borderRadius: 1 }}
              />
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setQrPreview(null)}>Đóng</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
