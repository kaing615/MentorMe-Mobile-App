import * as React from "react";
import {
  List,
  Datagrid,
  TextField,
  DateField,
  TextInput,
  SelectInput,
  EditButton,
  Edit,
  SimpleForm,
  TextInput as RaTextInput,
  SelectInput as RaSelectInput,
  useDataProvider,
  useNotify,
  useRefresh,
  useRecordContext,
  useRedirect,
  Confirm,
  Button,
  required,
} from "react-admin";

type Report = {
  id: string | number;
  type?: string; // spam, abuse, etc
  status?: "open" | "investigating" | "resolved";
  targetType?: "user" | "review" | "message" | "file";
  targetId?: string | number;
  reporterId?: string | number;
  createdAt?: string;
  note?: string;
};

const reportStatusChoices = [
  { id: "open", name: "open" },
  { id: "investigating", name: "investigating" },
  { id: "resolved", name: "resolved" },
];

const targetTypeChoices = [
  { id: "user", name: "user" },
  { id: "review", name: "review" },
  { id: "message", name: "message" },
  { id: "file", name: "file" },
];

const ReportFilters = [
  <TextInput key="q" source="q" label="Search" alwaysOn />,
  <SelectInput key="status" source="status" label="Status" choices={reportStatusChoices} />,
  <SelectInput key="targetType" source="targetType" label="Target" choices={targetTypeChoices} />,
];

function SetReportStatusButton(props: { toStatus: Report["status"]; label: string }) {
  const { toStatus, label } = props;
  const record = useRecordContext<Report>();
  const dataProvider = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();
  const [open, setOpen] = React.useState(false);

  if (!record) return null;

  const disabled = record.status === toStatus;

  const onConfirm = async () => {
    try {
      await dataProvider.update("reports", {
        id: record.id,
        data: { status: toStatus },
        previousData: record,
      });
      notify("Report updated", { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e?.message || "Update failed", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label={label} onClick={() => setOpen(true)} disabled={disabled} />
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

function GoToTargetButton() {
  const record = useRecordContext<Report>();
  const redirect = useRedirect();

  if (!record) return null;
  if (!record.targetType || !record.targetId) return null;

  const label =
    record.targetType === "user" ? "Open user" : `Open ${record.targetType}`;

  const onClick = () => {
    // Nếu targetType=user, chuyển sang resource users
    if (record.targetType === "user") {
      redirect(`/users/${record.targetId}`);
      return;
    }
    // Các loại khác tùy bạn có Resource tương ứng hay không
    redirect(`/${record.targetType}s/${record.targetId}`);
  };

  return <Button label={label} onClick={onClick} />;
}

export function ReportList() {
  return (
    <List filters={ReportFilters} sort={{ field: "createdAt", order: "DESC" }}>
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="type" />
        <TextField source="status" />
        <TextField source="targetType" />
        <TextField source="targetId" />
        <TextField source="reporterId" />
        <DateField source="createdAt" showTime />
        <EditButton />
        <GoToTargetButton />
        <SetReportStatusButton toStatus="investigating" label="Investigate" />
        <SetReportStatusButton toStatus="resolved" label="Resolve" />
      </Datagrid>
    </List>
  );
}

export function ReportEdit() {
  return (
    <Edit>
      <SimpleForm>
        <RaSelectInput source="status" choices={reportStatusChoices} validate={[required()]} />
        <RaTextInput source="type" fullWidth />
        <RaSelectInput source="targetType" choices={targetTypeChoices} />
        <RaTextInput source="targetId" />
        <RaTextInput source="reporterId" />
        <RaTextInput source="note" fullWidth multiline minRows={4} label="Admin note" />
      </SimpleForm>
    </Edit>
  );
}
