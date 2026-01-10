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
  FunctionField,
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
  { id: "open", name: "Mới" },
  { id: "investigating", name: "Đang xử lý" },
  { id: "resolved", name: "Đã xử lý" },
];

const statusLabelMap = statusChoices.reduce<Record<string, string>>((acc, choice) => {
  acc[choice.id] = choice.name;
  return acc;
}, {});

const targetTypeChoices = [
  { id: "user", name: "Người dùng" },
  { id: "review", name: "Đánh giá" },
  { id: "message", name: "Tin nhắn" },
  { id: "file", name: "Tệp" },
];

const targetTypeLabelMap = targetTypeChoices.reduce<Record<string, string>>((acc, choice) => {
  acc[choice.id] = choice.name;
  return acc;
}, {});

/* ===================== Filters ===================== */

const ReportFilters = [
  <TextInput key="q" source="q" label="Tìm kiếm" alwaysOn />,
  <SelectInput
    key="status"
    source="status"
    label="Trạng thái"
    choices={statusChoices}
  />,
  <SelectInput
    key="targetType"
    source="targetType"
    label="Đối tượng"
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
        }
      );

      if (!res.ok) throw new Error("Cập nhật thất bại");

      notify("Đã cập nhật báo cáo", { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e.message || "Cập nhật thất bại", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label={label} onClick={() => setOpen(true)} />
      <Confirm
        isOpen={open}
        title="Cập nhật trạng thái báo cáo"
        content={`Chuyển trạng thái thành "${statusLabelMap[toStatus ?? ""] || toStatus}"?`}
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

  if (!record || record.targetType !== "user" || !record.targetId) return null;

  const onClick = () => {
    redirect(`/users/${record.targetId}`);
  };

  return <Button label="Mở người dùng" onClick={onClick} />;
}

/* ===================== List ===================== */

export function ReportList() {
  return (
    <List
      filters={ReportFilters}
      sort={{ field: "createdAt", order: "DESC" }}
      perPage={25}
      title="Báo cáo"
    >
      <Datagrid rowClick={false}>
        <TextField source="id" label="Mã báo cáo" />
        <TextField source="type" label="Loại" />
        <FunctionField
          label="Trạng thái"
          render={(record: Report) =>
            record.status ? statusLabelMap[record.status] || record.status : "—"
          }
        />
        <FunctionField
          label="Đối tượng"
          render={(record: Report) =>
            record.targetType
              ? targetTypeLabelMap[record.targetType] || record.targetType
              : "—"
          }
        />
        <TextField source="targetId" label="ID đối tượng" />
        <TextField source="reporterId" label="Người báo cáo" />
        <DateField source="createdAt" showTime label="Tạo lúc" />
        <GoToTargetButton />
        <UpdateStatusButton toStatus="investigating" label="Đang xử lý" />
        <UpdateStatusButton toStatus="resolved" label="Đã xử lý" />
      </Datagrid>
    </List>
  );
}
