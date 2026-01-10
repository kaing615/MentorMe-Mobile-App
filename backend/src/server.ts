import cookieParser from "cookie-parser";
import cors from "cors";
import "dotenv/config";
import express, { Request, Response } from "express";
import helmet from "helmet";
import http from "http";
import mongoose from "mongoose";
import morgan from "morgan";
import path from "path";
import swaggerUi from "swagger-ui-express";
import { fileURLToPath } from "url";
import YAML from "yamljs";

import { startBookingJobs, stopBookingJobs } from "./jobs/booking.jobs";
import {
    startNotificationCleanupJobs,
    stopNotificationCleanupJobs,
} from "./jobs/notificationCleanup.jobs";
import routes from "./routes/index";
import { closeSocket, initSocket } from "./socket";
import { connectMongoDB } from "./utils/mongo";
import redis from "./utils/redis";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT: number = Number(process.env.PORT || 4000);

// Increase timeout for file uploads
app.use((req, res, next) => {
  if (req.path.includes('/upload')) {
    req.setTimeout(5 * 60 * 1000); // 5 minutes for upload routes
    res.setTimeout(5 * 60 * 1000);
  }
  next();
});

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
    await startNotificationCleanupJobs();

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

// Set server timeout to 5 minutes for file uploads
server.timeout = 5 * 60 * 1000; // 5 minutes
server.keepAliveTimeout = 5 * 60 * 1000;
server.headersTimeout = 5 * 60 * 1000;

startServer();

const shutdown = async () => {
  try {
    server.close(() => console.log("HTTP server closed"));
    await stopBookingJobs();
    await stopNotificationCleanupJobs();
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
