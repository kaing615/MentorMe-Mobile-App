import { readFileSync } from "fs";
import { join, dirname } from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

/**
 * Simple validation test for baseknowledge.json
 */
function validateBaseKnowledge() {
  console.log("🧪 Testing baseknowledge.json...\n");

  // Read the JSON file
  const filePath = join(__dirname, "baseknowledge.json");
  const rawData = readFileSync(filePath, "utf-8");
  const knowledgeBase = JSON.parse(rawData);

  // Check if it's an array
  if (!Array.isArray(knowledgeBase)) {
    console.error("❌ Knowledge base is not an array");
    return false;
  }

  console.log(`✅ Found ${knowledgeBase.length} knowledge entries\n`);

  // Validate each entry
  let errors = 0;
  knowledgeBase.forEach((entry, index) => {
    const issues: string[] = [];

    if (!entry.id) issues.push("Missing 'id'");
    if (!entry.title) issues.push("Missing 'title'");
    if (!entry.content) issues.push("Missing 'content'");
    if (!entry.tags || !Array.isArray(entry.tags) || entry.tags.length === 0) {
      issues.push("Missing or empty 'tags'");
    }

    if (issues.length > 0) {
      console.error(`❌ Entry ${index + 1} (${entry.id || "unknown"}):`);
      issues.forEach((issue) => console.error(`   - ${issue}`));
      errors++;
    }
  });

  if (errors === 0) {
    console.log("✅ All entries are valid!\n");

    // Print summary
    console.log("📊 Summary:");
    console.log(`   - Total entries: ${knowledgeBase.length}`);

    const allTags = new Set<string>();
    knowledgeBase.forEach((entry) => {
      entry.tags.forEach((tag) => allTags.add(tag));
    });
    console.log(`   - Unique tags: ${allTags.size}`);

    // List all categories
    console.log(`\n📚 Categories (tags):`);
    Array.from(allTags)
      .sort()
      .forEach((tag) => console.log(`   - ${tag}`));

    return true;
  } else {
    console.error(`\n❌ Found ${errors} entries with issues`);
    return false;
  }
}

// Run validation
const isValid = validateBaseKnowledge();
process.exit(isValid ? 0 : 1);
