import { Admin, Resource } from "react-admin";
import { authProvider } from "./authProvider";
import { dataProvider } from "./dataProvider";

import { BookingEdit, BookingList } from "./resources/bookings";
import { MentorAppEdit, MentorAppList } from "./resources/mentorApplications";
import { ReportEdit, ReportList } from "./resources/reports";
import { UserEdit, UserList } from "./resources/users";

export default function App() {
  return (
    <Admin dataProvider={dataProvider} authProvider={authProvider}>
      <Resource name="users" list={UserList} edit={UserEdit} />
      <Resource name="bookings" list={BookingList} edit={BookingEdit} />
      <Resource name="reports" list={ReportList} edit={ReportEdit} />
    </Admin>
  );
}
