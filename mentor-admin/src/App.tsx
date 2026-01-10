import { Admin, CustomRoutes, Resource } from "react-admin";
import { Route } from "react-router-dom";
import { authProvider } from "./authProvider";
import { Dashboard } from "./Dashboard";
import { dataProvider } from "./dataProvider";
import { Layout } from "./Layout";
import { LoginPage } from "./LoginPage";
import { darkTheme, lightTheme } from "./theme";
import { i18nProvider } from "./i18nProvider";

import {
  BookingEdit,
  BookingList,
  MentorApplicationEdit,
  MentorApplicationList,
  PayoutList,
  ReportList,
  SessionList,
  TopUpList,
  UserEdit,
  UserList,
  WebhookPage,
} from "./resources";

import { MyProfile } from "./MyProfile";

export default function App() {
  return (
    <Admin
      dataProvider={dataProvider}
      authProvider={authProvider}
      layout={Layout}
      theme={lightTheme}
      darkTheme={darkTheme}
      dashboard={Dashboard}
      loginPage={LoginPage}
      i18nProvider={i18nProvider}
      requireAuth
    >
      <Resource
        name="users"
        options={{ label: "Người dùng" }}
        list={UserList}
        edit={UserEdit}
      />

      <Resource
        name="admin/bookings"
        options={{ label: "Lịch hẹn" }}
        list={BookingList}
        edit={BookingEdit}
      />

      <Resource
        name="admin/sessions"
        options={{ label: "Phiên học" }}
        list={SessionList}
      />

      <Resource name="reports" options={{ label: "Báo cáo" }} list={ReportList} />

      {/* Admin only */}
      <Resource
        name="admin/payouts"
        options={{ label: "Yêu cầu rút tiền" }}
        list={PayoutList}
      />

      <Resource
        name="admin/webhook-logs"
        options={{ label: "Webhook giả lập" }}
        list={WebhookPage}
      />

      <Resource
        name="admin/topup-intents"
        options={{ label: "Yêu cầu nạp tiền" }}
        list={TopUpList}
      />

      <Resource
        name="mentor-applications"
        options={{ label: "Đơn đăng ký mentor" }}
        list={MentorApplicationList}
        edit={MentorApplicationEdit}
      />

      <CustomRoutes>
        <Route path="/my-profile" element={<MyProfile />} />
      </CustomRoutes>
    </Admin>
  );
}
