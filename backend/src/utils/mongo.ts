import mongoose from "mongoose";
import dotenv from "dotenv";

dotenv.config();

const mongoURL = process.env.MONGO_URL || "mongodb://localhost:27017/mentorme";

export const connectMongoDB = async () => {
  try {
    await mongoose.connect(mongoURL);
    console.log("MongoDB connected successfully");
  } catch (err: any) {
    console.error("MongoDB connection failed:", err.message);
    process.exit(1);
  }
};

export const removeAllCollections = async () => {
  if (mongoose.connection.readyState !== 1) {
    await connectMongoDB();
  }
  const db = mongoose.connection.db;
  const name = mongoose.connection.name;

  if (!db) {
    throw new Error("MongoDB connection not initialized");
  }

  const cols = await db.collections();
  for (const c of cols) {
    await c.deleteMany({});
  }
  console.log(`Cleared ${cols.length} collections in DB ${name}`);
};

export const dropAllCollections = async () => {
  const collections = Object.keys(mongoose.connection.collections);
  for (const collectionName of collections) {
    try {
      await mongoose.connection.dropCollection(collectionName);
      console.log(`Dropped ${collectionName} collection`);
    } catch (err: any) {
      if (err.message.includes("ns not found")) continue;
      console.error(
        `Failed to drop ${collectionName} collection:`,
        err.message
      );
    }
  }
};
