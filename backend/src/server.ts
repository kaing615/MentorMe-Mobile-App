import "dotenv/config";
import express, { Request, Response } from "express";
import cors from "cors";
import cookieParser from "cookie-parser";
import helmet from "helmet";
import morgan from "morgan";
import swaggerUi from "swagger-ui-express";
import YAML from "yamljs";
import path from "path";
import { fileURLToPath } from "url";
import http from "http";
import mongoose from "mongoose";

import routes from "./routes/index";
import redis from "./utils/redis";
import { connectMongoDB } from "./utils/mongo";
import { startBookingJobs, stopBookingJobs } from "./jobs/booking.jobs";
import { initSocket, closeSocket } from "./socket";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT: number = Number(process.env.PORT || 4000);

app.use(cors());
app.use(helmet());
app.use(helmet.crossOriginResourcePolicy({ policy: "cross-origin" }));
app.use(morgan("common"));
app.use(express.json({ limit: "30mb" }));
app.use(express.urlencoded({ limit: "30mb", extended: true }));
app.use(cookieParser());

const swaggerDocument = YAML.load(path.join(__dirname, "swagger.yaml"));
app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerDocument));

app.use("/api/v1", routes);
app.get("/", (_req: Request, res: Response) => {
  res.send("Welcome to the MentorMe Mobile backend!");
});

app.get("/health", (_req, res) => {
  res.json({
    status: "ok",
    redis: redis.isOpen,
    mongo: mongoose.connection.readyState,
  });
});

async function startServer() {
  try {
    await redis.connect();
    console.log("Redis connected");

    await connectMongoDB();

    await startBookingJobs();

    await initSocket(server);

    server.listen(PORT, () => {
      console.log(`Server is running on http://localhost:${PORT}`);
      console.log(`API docs: http://localhost:${PORT}/api-docs`);
      console.log("🔑 GOOGLE_API_KEY =", process.env.GOOGLE_API_KEY);
    });
  } catch (err) {
    console.error("Failed to start server:", err);
    process.exit(1);
  }
}

const server = http.createServer(app);
startServer();

const shutdown = async () => {
  try {
    server.close(() => console.log("HTTP server closed"));
    await stopBookingJobs();
    await closeSocket();
    if (redis.isOpen) await redis.quit();
    if (mongoose.connection.readyState === 1) await mongoose.connection.close();
    console.log("Cleaned up Redis and MongoDB connections");
  } finally {
    process.exit(0);
  }
};

process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);
