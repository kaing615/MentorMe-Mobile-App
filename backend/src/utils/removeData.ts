import "dotenv/config";
import mongoose from "mongoose";
import { connectMongoDB, removeAllCollections } from "./mongo";

const run = async () => {
  try {
    await connectMongoDB();
    console.log(
      "Connected to",
      mongoose.connection.host,
      "DB:",
      mongoose.connection.name
    );

    await removeAllCollections();
    console.log("Data removed successfully.");
  } catch (err) {
    console.error("Error removing data:", err);
    process.exit(1);
  } finally {
    await mongoose.disconnect();
    process.exit(0);
  }
};

run();
