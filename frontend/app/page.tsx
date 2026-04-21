import Navbar from "@/components/Navbar";
import Hero from "@/components/Hero";
import Features from "@/components/Features";
import HowItWorks from "@/components/HowItWorks";
import ScreenshotCarousel from "@/components/ScreenshotCarousel";
import SocialProof from "@/components/SocialProof";
import Pricing from "@/components/Pricing";
import EarlyAccess from "@/components/EarlyAccess";
import Download from "@/components/Download";
import Footer from "@/components/Footer";

export default function Home() {
  return (
    <main className="relative overflow-x-hidden">
      <Navbar />
      <Hero />
      <Features />
      <HowItWorks />
      <ScreenshotCarousel />
      <SocialProof />
      <Pricing />
      <EarlyAccess />
      <Download />
      <Footer />
    </main>
  );
}
