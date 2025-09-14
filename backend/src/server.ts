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

import routes from "./routes/index";
import redis from "./utils/redis";
import requestHandlerPostgres from "./utils/postgres";

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
app.get("/pg-version", requestHandlerPostgres);
app.get("/", (_req: Request, res: Response) => {
  res.send("Welcome to the MentorMe Mobile backend!");
});

const server = app.listen(PORT, async () => {
  await redis.connect();
  console.log("Redis connected");
  console.log(`🚀 Server is running on http://localhost:${PORT}`);
  console.log(`📖 API docs: http://localhost:${PORT}/api-docs`);
});

const shutdown = () => server.close(() => process.exit(0));
process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);
