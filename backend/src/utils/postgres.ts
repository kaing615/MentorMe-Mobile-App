import dotenv from "dotenv";
import { neon } from "@neondatabase/serverless";
import { Request, Response } from "express";

dotenv.config();

export const sql = neon(process.env.DATABASE_URL!);

const requestHandlerPostgres = async (
  req: Request,
  res: Response
): Promise<void> => {
  const result = await sql`SELECT version()`;
  const { version } = result[0];
  res.writeHead(200, { "Content-Type": "text/plain" });
  res.end(version);
};

export default requestHandlerPostgres;
