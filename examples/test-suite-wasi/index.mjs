import { readFileSync } from "node:fs";

const args = process.argv.slice(2);

if (args.length === 0) {
  console.log("Usage: index.mjs <path to wasm module>");
  process.exit(1);
}
const buf = readFileSync(args[0]);
await WebAssembly.instantiate(buf, {});
