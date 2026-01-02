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
  useNotify,
  useRefresh,
  useRecordContext,
  useRedirect,
} from "react-admin";

/* ===================== Types ===================== */

type Report = {
  id: string | number;
  type?: string;
  status?: "open" | "investigating" | "resolved";
  targetType?: "user" | "review" | "message" | "file";
  targetId?: string | number;
  reporterId?: string | number;
  note?: string;
  createdAt?: string;
};

/* ===================== Constants ===================== */

const statusChoices = [
  { id: "open", name: "Open" },
  { id: "investigating", name: "Investigating" },
  { id: "resolved", name: "Resolved" },
];

const targetTypeChoices = [
  { id: "user", name: "User" },
  { id: "review", name: "Review" },
  { id: "message", name: "Message" },
  { id: "file", name: "File" },
];

/* ===================== Filters ===================== */

const ReportFilters = [
  <TextInput key="q" source="q" label="Search" alwaysOn />,
  <SelectInput
    key="status"
    source="status"
    label="Status"
    choices={statusChoices}
  />,
  <SelectInput
    key="targetType"
    source="targetType"
    label="Target"
    choices={targetTypeChoices}
  />,
];

/* ===================== Action Buttons ===================== */

function UpdateStatusButton({
  toStatus,
  label,
}: {
  toStatus: Report["status"];
  label: string;
}) {
  const record = useRecordContext<Report>();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);

  if (!record) return null;
  if (record.status === toStatus) return null;

  const onConfirm = async () => {
    try {
      const res = await fetch(
        `${import.meta.env.VITE_API_URL}/reports/${record.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("access_token")}`,
          },
          body: JSON.stringify({ status: toStatus }),
        },
      );

      if (!res.ok) throw new Error("Update failed");

      notify("Report updated", { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Update failed", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label={label} onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        title="Update report status"
        content={`Set status to "${toStatus}"?`}
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

/* ===================== Go To Target ===================== */

function GoToTargetButton() {
  const record = useRecordContext<Report>();
  const redirect = useRedirect();

  if (!record || !record.targetType || !record.targetId) return null;

  const label =
    record.targetType === "user" ? "Open User" : `Open ${record.targetType}`;

  const onClick = () => {
    if (record.targetType === "user") {
      redirect(`/users/${record.targetId}`);
    } else {
      redirect(`/${record.targetType}s/${record.targetId}`);
    }
  };

  return <Button label={label} onClick={onClick} />;
}

/* ===================== List ===================== */

export function ReportList() {
  return (
    <List
      filters={ReportFilters}
      sort={{ field: "createdAt", order: "DESC" }}
      perPage={25}
      title="Reports"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="type" />
        <TextField source="status" />
        <TextField source="targetType" />
        <TextField source="targetId" />
        <TextField source="reporterId" />
        <DateField source="createdAt" showTime />
        <GoToTargetButton />
        <UpdateStatusButton toStatus="investigating" label="Investigate" />
        <UpdateStatusButton toStatus="resolved" label="Resolve" />
      </Datagrid>
    </List>
  );
}
