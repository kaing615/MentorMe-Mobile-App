import * as React from "react";
import {
  Button,
  Confirm,
  Datagrid,
  DateField,
  Edit,
  EditButton,
  List,
  SelectInput as RaSelectInput,
  TextInput as RaTextInput,
  required,
  SelectInput,
  SimpleForm,
  TextField,
  TextInput,
  useDataProvider,
  useNotify,
  useRecordContext,
  useRefresh,
} from "react-admin";

type MentorApplication = {
  id: string | number;
  mentorId?: string | number;
  fullName?: string;
  title?: string;
  status?: "pending" | "approved" | "rejected";
  submittedAt?: string;
  note?: string;
};

const statusChoices = [
  { id: "pending", name: "pending" },
  { id: "approved", name: "approved" },
  { id: "rejected", name: "rejected" },
];

const MentorAppFilters = [
  <TextInput key="q" source="q" label="Search" alwaysOn />,
  <SelectInput key="status" source="status" label="Status" choices={statusChoices} />,
];

function UpdateStatusButton(props: {
  toStatus: "approved" | "rejected";
  confirmTitle: string;
  confirmContent: string;
}) {
  const { toStatus, confirmTitle, confirmContent } = props;
  const record = useRecordContext<MentorApplication>();
  const dataProvider = useDataProvider();
  const notify = useNotify();
  const refresh = useRefresh();

  const [open, setOpen] = React.useState(false);

  if (!record) return null;

  const disabled =
    record.status === toStatus || record.status === "approved" || record.status === "rejected";

  const onConfirm = async () => {
    try {
      await dataProvider.update("mentor-applications", {
        id: record.id,
        data: { status: toStatus },
        previousData: record,
      });
      notify(`Application ${toStatus}`, { type: "success" });
      refresh();
    } catch (e: any) {
      notify(e?.message || "Update failed", { type: "error" });
    } finally {
      setOpen(false);
    }
  };

  return (
    <>
      <Button label={toStatus === "approved" ? "Approve" : "Reject"} onClick={() => setOpen(true)} disabled={disabled} />
      <Confirm
        isOpen={open}
        title={confirmTitle}
        content={confirmContent}
        onConfirm={onConfirm}
        onClose={() => setOpen(false)}
      />
    </>
  );
}

export function MentorAppList() {
  return (
    <List
      filters={MentorAppFilters}
      sort={{ field: "submittedAt", order: "DESC" }}
    >
      <Datagrid rowClick={false}>
        <TextField source="id" />
        <TextField source="mentorId" />
        <TextField source="fullName" label="Name" />
        <TextField source="title" label="Title" />
        <TextField source="status" />
        <DateField source="submittedAt" showTime />
        <EditButton />
        <UpdateStatusButton
          toStatus="approved"
          confirmTitle="Approve mentor application"
          confirmContent="Approve this mentor application?"
        />
        <UpdateStatusButton
          toStatus="rejected"
          confirmTitle="Reject mentor application"
          confirmContent="Reject this mentor application?"
        />
      </Datagrid>
    </List>
  );
}

export function MentorAppEdit() {
  return (
    <Edit>
      <SimpleForm>
        <RaSelectInput source="status" choices={statusChoices} validate={[required()]} />
        <RaTextInput source="fullName" fullWidth />
        <RaTextInput source="title" fullWidth />
        <RaTextInput source="note" fullWidth multiline minRows={3} label="Admin note" />
      </SimpleForm>
    </Edit>
  );
}
