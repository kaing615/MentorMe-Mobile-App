// src/resources/topups.tsx
import * as React from "react";
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { useNotify } from "react-admin";

const apiUrl = import.meta.env.VITE_API_URL;

type TopUpItem = {
  id: string;
  amount: number;
  currency?: string;
  note?: string;
  status?: string;
  qrImageUrl?: string | null;
  referenceCode?: string;
  createdAt?: string;
  user?: { id?: string; userName?: string; email?: string } | any;
};

export const TopUpList = () => {
  const notify = useNotify();
  const [loading, setLoading] = React.useState(false);
  const [items, setItems] = React.useState<TopUpItem[]>([]);
  const [selectedQr, setSelectedQr] = React.useState<string | null>(null);

  const token = localStorage.getItem("access_token") || "";

  const fetchList = React.useCallback(async () => {
    setLoading(true);
    try {
      // Correct backend route:
      const url = `${apiUrl}/wallet/admin/topups/pending`;
      const res = await fetch(url, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: "application/json",
        },
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err?.message || `Fetch failed (${res.status})`);
      }
      const json = await res.json();
      // backend returns { items } (or maybe { data: { items } })
      const list = json?.data?.items ?? json?.items ?? json?.data ?? json;
      const mapped = Array.isArray(list)
        ? list.map((it: any) => ({ id: it.id ?? it._id, ...it }))
        : [];
      setItems(mapped);
    } catch (e: any) {
      notify(e.message || "Lỗi khi tải danh sách topup", { type: "error" });
    } finally {
      setLoading(false);
    }
  }, [token, notify]);

  React.useEffect(() => {
    fetchList();
  }, [fetchList]);

  const postAction = async (id: string, action: "approve" | "reject", reason?: string) => {
    try {
      // Correct admin endpoints:
      const url =
        action === "approve"
          ? `${apiUrl}/wallet/admin/topups/${id}/approve`
          : `${apiUrl}/wallet/admin/topups/${id}/reject`;

      const res = await fetch(url, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: action === "reject" ? JSON.stringify({ reason }) : undefined,
      });

      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err?.message || `Action failed (${res.status})`);
      }

      notify(`Đã ${action === "approve" ? "duyệt" : "từ chối"} topup`, { type: "success" });
      // refresh list
      await fetchList();
    } catch (e: any) {
      notify(e.message || "Lỗi khi thực hiện action", { type: "error" });
    }
  };

  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h5" gutterBottom>
        Duyệt TopUp Intent
      </Typography>

      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="body2" color="text.secondary">
            Hiển thị yêu cầu nạp tiền đang chờ. Bấm Approve để cộng tiền vào ví, Reject để từ chối.
          </Typography>
        </CardContent>
      </Card>

      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Người yêu cầu</TableCell>
              <TableCell>Mã tham chiếu</TableCell>
              <TableCell>Số tiền</TableCell>
              <TableCell>Ghi chú</TableCell>
              <TableCell>Trạng thái</TableCell>
              <TableCell>Ngày tạo</TableCell>
              <TableCell align="right">Hành động</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {items.length === 0 && (
              <TableRow>
                <TableCell colSpan={8}>
                  <Typography align="center">Không có yêu cầu nào</Typography>
                </TableCell>
              </TableRow>
            )}

            {items.map((it) => (
              <TableRow key={it.id}>
                <TableCell>
                  {it.user?.userName ?? it.user?.email ?? "—"}
                </TableCell>
                <TableCell>{it.referenceCode ?? "—"}</TableCell>
                <TableCell>{(it.amount ?? 0).toLocaleString()} {it.currency ?? "VND"}</TableCell>
                <TableCell>{it.note ?? "—"}</TableCell>
                <TableCell>{it.status ?? "—"}</TableCell>
                <TableCell>{it.createdAt ? new Date(it.createdAt).toLocaleString() : "—"}</TableCell>
                <TableCell align="right">
                  <Box sx={{ display: "flex", gap: 1, justifyContent: "flex-end" }}>
                    <Button
                      size="small"
                      variant="contained"
                      onClick={() => {
                        if (confirm("Xác nhận duyệt yêu cầu này?")) postAction(it.id, "approve");
                      }}
                    >
                      Approve
                    </Button>
                    <Button
                      size="small"
                      variant="outlined"
                      onClick={() => {
                        if (confirm("Xác nhận từ chối yêu cầu này?")) postAction(it.id, "reject");
                      }}
                    >
                      Reject
                    </Button>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Box>
  );
};
