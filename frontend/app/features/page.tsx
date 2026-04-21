import Navbar from "@/components/Navbar";
import Footer from "@/components/Footer";
import AllFeatures from "@/components/AllFeatures";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Features — STAX",
  description:
    "Every feature in STAX: AI chip counting, session management, casino finder, P&L tracking, hand history, and more.",
};

export default function FeaturesPage() {
  return (
    <main className="relative overflow-x-hidden">
      <Navbar />
      <div className="pt-[68px]">
        <AllFeatures />
      </div>
      <Footer />
    </main>
  );
}
