import path from "node:path";
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  turbopack: {
    // Pin the workspace root so Turbopack doesn't crawl past frontend/ and
    // land on the bare repo-root package.json (which has no node_modules).
    root: path.resolve(__dirname),
    // Explicitly alias tailwindcss to the local install. The CSS @import
    // "tailwindcss" resolver bypasses the root setting and walks up to the
    // nearest package.json — which is the repo root, not frontend/. This
    // alias short-circuits that so Turbopack always finds the right copy.
    resolveAlias: {
      tailwindcss: path.resolve(__dirname, "node_modules/tailwindcss"),
    },
  },
};

export default nextConfig;
