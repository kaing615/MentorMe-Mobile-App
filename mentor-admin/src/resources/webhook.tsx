import {
  Card,
  CardContent,
  Button,
  TextField,
  Typography,
  Stack,
} from "@mui/material";
import { useNotify, useRefresh } from "react-admin";
import { useState } from "react";

export const WebhookPage = () => {
  const notify = useNotify();
  const refresh = useRefresh();

  const [externalId, setExternalId] = useState("");
  const [status, setStatus] = useState<"PAID" | "FAILED">("PAID");
  const [loading, setLoading] = useState(false);

  const sendWebhook = async () => {
    if (!externalId) {
      notify("Vui lòng nhập externalId", { type: "warning" });
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(
        `${import.meta.env.VITE_API_URL}/webhooks/payout-provider`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            externalId,
            status,
          }),
        }
      );

      if (!res.ok) {
        const text = await res.text();
        throw new Error(text);
      }

      notify(`Đã gửi webhook: ${status}`, { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Gửi webhook thất bại", { type: "error" });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Webhook giả lập chi trả
        </Typography>

        <Stack spacing={2} maxWidth={400}>
          <TextField
            label="External ID"
            placeholder="PO-xxxxxxxx"
            value={externalId}
            onChange={(e) => setExternalId(e.target.value)}
            fullWidth
          />

          <Stack direction="row" spacing={2}>
            <Button
              variant={status === "PAID" ? "contained" : "outlined"}
              color="success"
              onClick={() => setStatus("PAID")}
            >
              Đã chi trả
            </Button>

            <Button
              variant={status === "FAILED" ? "contained" : "outlined"}
              color="error"
              onClick={() => setStatus("FAILED")}
            >
              Thất bại
            </Button>
          </Stack>

          <Button variant="contained" onClick={sendWebhook} disabled={loading}>
            Gửi webhook
          </Button>
        </Stack>
      </CardContent>
    </Card>
  );
};
