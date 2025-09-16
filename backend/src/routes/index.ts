import { Router } from "express";
import userRoute from "./user.router";

const router = Router();

router.use("/users", userRoute);

export default router;
