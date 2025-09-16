import { Router } from "express";
import userRoute from "./user.router";

const router = Router();

router.use("/auth", userRoute);

export default router;
