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
  { id: "pending", name: "Chờ duyệt" },
  { id: "approved", name: "Đã duyệt" },
  { id: "rejected", name: "Từ chối" },
];

const statusLabelMap = statusChoices.reduce<Record<string, string>>((acc, choice) => {
  acc[choice.id] = choice.name;
  return acc;
}, {});

const apiUrl = import.meta.env.VITE_API_URL;
const token = () => localStorage.getItem("access_token") || "";

/* ===================== Filters ===================== */

const MentorAppFilters = [
  <TextInput key="q" source="q" label="Tìm kiếm" alwaysOn />,
  <SelectInput
    key="status"
    source="status"
    label="Trạng thái"
    choices={statusChoices}
  />,
];

/* ===================== UI Helpers ===================== */

function StatusChip({ label: _label }: { label?: string }) {
  const record = useRecordContext<MentorApplication>();
  if (!record?.status) return null;

  const colorMap: Record<string, "warning" | "success" | "error"> = {
    pending: "warning",
    approved: "success",
    rejected: "error",
  };

  return (
    <Chip
      size="small"
      label={statusLabelMap[record.status] || record.status}
      color={colorMap[record.status]}
    />
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
        }
      );

      if (!res.ok) throw new Error("Duyệt thất bại");

      notify("Đã duyệt đơn đăng ký mentor", { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Duyệt thất bại", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label="Duyệt" onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        title="Duyệt đơn đăng ký mentor"
        content="Duyệt đơn đăng ký này?"
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
      notify("Vui lòng nhập lý do từ chối", { type: "warning" });
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
        }
      );

      if (!res.ok) throw new Error("Từ chối thất bại");

      notify("Đã từ chối đơn đăng ký mentor", { type: "info" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Từ chối thất bại", { type: "error" });
    } finally {
      setOpen(false);
      setReason("");
    }
  };

  return (
    <>
      <Button label="Từ chối" onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        title="Từ chối đơn đăng ký mentor"
        content={
          <textarea
            placeholder="Lý do (bắt buộc)"
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
      title="Đơn đăng ký mentor"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" label="Mã đơn" />
        <TextField source="mentorId" label="ID mentor" />
        <TextField source="fullName" label="Họ tên" />
        <TextField source="jobTitle" label="Chức danh" />
        <TextField source="category" label="Lĩnh vực" />
        <StatusChip label="Trạng thái" />
        <DateField source="submittedAt" showTime label="Nộp lúc" />
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
          label="Trạng thái"
          choices={statusChoices}
          validate={[required()]}
        />
        <TextInput source="fullName" label="Họ tên" fullWidth />
        <TextInput source="jobTitle" label="Chức danh" fullWidth />
        <TextInput source="category" label="Lĩnh vực" fullWidth />
        <TextInput
          source="note"
          label="Ghi chú admin"
          fullWidth
          multiline
          minRows={4}
        />
      </SimpleForm>
    </Edit>
  );
}
