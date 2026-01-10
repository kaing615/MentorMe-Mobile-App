import * as React from "react";
import {
  List,
  Datagrid,
  TextField,
  DateField,
  TextInput,
  SelectInput,
  Button,
  Confirm,
  Edit,
  EditButton,
  SimpleForm,
  useNotify,
  useRefresh,
  useRecordContext,
  required,
} from "react-admin";
import { Chip } from "@mui/material";

/* ===================== Types ===================== */

type MentorApplication = {
  id: string | number;
  mentorId?: string | number;
  fullName?: string;
  jobTitle?: string;
  category?: string;
  status?: "pending" | "approved" | "rejected";
  submittedAt?: string;
  note?: string;
};

/* ===================== Constants ===================== */

const statusChoices = [
  { id: "pending", name: "Pending" },
  { id: "approved", name: "Approved" },
  { id: "rejected", name: "Rejected" },
];

const apiUrl = import.meta.env.VITE_API_URL;
const token = () => localStorage.getItem("access_token") || "";

/* ===================== Filters ===================== */

const MentorAppFilters = [
  <TextInput key="q" source="q" label="Search" alwaysOn />,
  <SelectInput
    key="status"
    source="status"
    label="Status"
    choices={statusChoices}
  />,
];

/* ===================== UI Helpers ===================== */

function StatusChip() {
  const record = useRecordContext<MentorApplication>();
  if (!record?.status) return null;

  const colorMap: any = {
    pending: "warning",
    approved: "success",
    rejected: "error",
  };

  return (
    <Chip size="small" label={record.status} color={colorMap[record.status]} />
  );
}

/* ===================== Action Buttons ===================== */

function ApproveButton() {
  const record = useRecordContext<MentorApplication>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);

  if (!record || record.status !== "pending") return null;

  const onConfirm = async () => {
    try {
      const res = await fetch(
        `${apiUrl}/mentor-applications/${record.id}/approve`,
        {
          method: "PUT",
          headers: {
            Authorization: `Bearer ${token()}`,
          },
        },
      );

      if (!res.ok) throw new Error("Approve failed");

      notify("Mentor application approved", { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Approve failed", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label="Approve" onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        title="Approve mentor application"
        content="Approve this mentor application?"
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

function RejectButton() {
  const record = useRecordContext<MentorApplication>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);
  const [reason, setReason] = React.useState("");

  if (!record || record.status !== "pending") return null;

  const onConfirm = async () => {
    if (!reason.trim()) {
      notify("Please provide a rejection reason", { type: "warning" });
      return;
    }

    try {
      const res = await fetch(
        `${apiUrl}/mentor-applications/${record.id}/reject`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token()}`,
          },
          body: JSON.stringify({ reason }),
        },
      );

      if (!res.ok) throw new Error("Reject failed");

      notify("Mentor application rejected", { type: "info" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Reject failed", { type: "error" });
    } finally {
      setOpen(false);
      setReason("");
    }
  };

  return (
    <>
      <Button label="Reject" onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        title="Reject mentor application"
        content={
          <textarea
            placeholder="Reason (required)"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={4}
            style={{ width: "100%" }}
          />
        }
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

/* ===================== List ===================== */

export function MentorApplicationList() {
  return (
    <List
      filters={MentorAppFilters}
      sort={{ field: "submittedAt", order: "DESC" }}
      perPage={25}
      title="Mentor Applications"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="mentorId" />
        <TextField source="fullName" />
        <TextField source="jobTitle" />
        <TextField source="category" />
        <StatusChip />
        <DateField source="submittedAt" showTime />
        <EditButton />
        <ApproveButton />
        <RejectButton />
      </Datagrid>
    </List>
  );
}

/* ===================== Edit ===================== */

export function MentorApplicationEdit() {
  return (
    <Edit>
      <SimpleForm>
        <SelectInput
          source="status"
          choices={statusChoices}
          validate={[required()]}
        />
        <TextInput source="fullName" fullWidth />
        <TextInput source="jobTitle" fullWidth />
        <TextInput source="category" fullWidth />
        <TextInput
          source="note"
          fullWidth
          multiline
          minRows={4}
          label="Admin note"
        />
      </SimpleForm>
    </Edit>
  );
}
