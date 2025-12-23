// src/resources/payouts.tsx
import * as React from "react";
import {
  List,
  Datagrid,
  TextField,
  NumberField,
  DateField,
  TextInput,
  SelectInput,
  Button,
  Confirm,
  useNotify,
  useRefresh,
  useRecordContext,
} from "react-admin";

const apiUrl = import.meta.env.VITE_API_URL;

// Kiểu dữ liệu payout (theo DTO BE trả về)
type Payout = {
  id: string;
  mentorId: string;
  amount: number;
  currency: "VND" | "USD";
  status: "PENDING" | "PROCESSING" | "PAID" | "FAILED" | string;
  attemptCount: number;
  externalId?: string | null;
  createdAt: string;
  updatedAt: string;
};

const payoutStatusChoices = [
  { id: "PENDING", name: "PENDING" },
  { id: "PROCESSING", name: "PROCESSING" },
  { id: "PAID", name: "PAID" },
  { id: "FAILED", name: "FAILED" },
];

const PayoutFilters = [
  <TextInput key="mentorId" source="mentorId" label="Mentor ID" />,
  <SelectInput
    key="status"
    source="status"
    label="Status"
    choices={payoutStatusChoices}
    alwaysOn
  />,
];

// Helper call API giống pattern trong users.tsx
async function callPayoutAction(path: string, method: "POST") {
  const res = await fetch(`${apiUrl}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${localStorage.getItem("access_token") || ""}`,
    },
  });

  const body = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(body.message || `Request failed (${res.status})`);
  }
  return body;
}

// Nút Approve
function ApprovePayoutButton() {
  const record = useRecordContext<Payout>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);
  const [loading, setLoading] = React.useState(false);

  if (!record) return null;
  if (record.status !== "PENDING") return null;

  const onConfirm = async () => {
    setLoading(true);
    try {
      await callPayoutAction(`/admin/payouts/${record.id}/approve`, "POST");
      notify("Payout approved", { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Approve failed", { type: "error" });
    } finally {
      setLoading(false);
      setOpen(false);
    }
  };

  return (
    <>
      <Button label="Approve" onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        loading={loading}
        title="Approve payout"
        content="Approve this payout request and debit mentor wallet?"
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

// Nút Retry
function RetryPayoutButton() {
  const record = useRecordContext<Payout>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);
  const [loading, setLoading] = React.useState(false);

  if (!record) return null;
  if (record.status !== "FAILED" && record.status !== "PROCESSING") return null;

  const onConfirm = async () => {
    setLoading(true);
    try {
      await callPayoutAction(`/admin/payouts/${record.id}/retry`, "POST");
      notify("Payout retry triggered", { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Retry failed", { type: "error" });
    } finally {
      setLoading(false);
      setOpen(false);
    }
  };

  return (
    <>
      <Button label="Retry" onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        loading={loading}
        title="Retry payout"
        content="Retry this payout with the provider?"
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

export function PayoutList() {
  return (
    <List
      filters={PayoutFilters}
      sort={{ field: "createdAt", order: "DESC" }}
      perPage={25}
    >
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="mentorId" />
        <NumberField source="amount" />
        <TextField source="currency" />
        <TextField source="status" />
        <NumberField source="attemptCount" />
        <TextField source="externalId" />
        <DateField source="createdAt" showTime />
        <DateField source="updatedAt" showTime />
        <ApprovePayoutButton />
        <RetryPayoutButton />
      </Datagrid>
    </List>
  );
}
