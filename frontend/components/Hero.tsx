import Link from "next/link";
import Image from "next/image";
import PhoneMockup from "./PhoneMockup";
import QRCodeDisplay from "./QRCodeDisplay";

export default function Hero() {
  return (
    <section className="relative min-h-[100svh] flex items-center overflow-hidden">

      {/* ── Layer 0: Base ──────────────────────────────────────── */}
      <div className="absolute inset-0" style={{ background: "#070709" }} />

      {/* ── Layer 1: Multi-point lighting ─────────────────────── */}
      <div
        className="absolute pointer-events-none"
        style={{
          right: "-20%",
          top: "-15%",
          width: "90%",
          height: "70%",
          maxWidth: 800,
          maxHeight: 700,
          background:
            "radial-gradient(ellipse at center, rgba(124,108,246,0.2) 0%, rgba(91,79,212,0.08) 45%, transparent 72%)",
        }}
      />
      <div
        className="absolute pointer-events-none hidden sm:block"
        style={{
          left: "-10%",
          bottom: "0%",
          width: 500,
          height: 400,
          background:
            "radial-gradient(ellipse at center, rgba(90,200,250,0.055) 0%, transparent 70%)",
        }}
      />
      <div
        className="absolute pointer-events-none"
        style={{
          left: "10%",
          bottom: "-10%",
          width: "80%",
          height: 250,
          maxWidth: 600,
          background:
            "radial-gradient(ellipse at center, rgba(191,90,242,0.06) 0%, transparent 70%)",
          filter: "blur(8px)",
        }}
      />

      {/* ── Layer 2: Grid with radial vignette ────────────────── */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          backgroundImage:
            "linear-gradient(rgba(58,58,66,0.14) 1px, transparent 1px), linear-gradient(90deg, rgba(58,58,66,0.14) 1px, transparent 1px)",
          backgroundSize: "44px 44px",
          maskImage:
            "radial-gradient(ellipse 90% 80% at 50% 40%, black 20%, transparent 100%)",
          WebkitMaskImage:
            "radial-gradient(ellipse 90% 80% at 50% 40%, black 20%, transparent 100%)",
        }}
      />

      {/* ── Layer 3: Film grain ───────────────────────────────── */}
      <div className="hero-noise" />

      {/* ── Content ───────────────────────────────────────────── */}
      <div className="relative w-full max-w-6xl mx-auto px-5 sm:px-6 pt-24 pb-14 sm:pt-28 sm:pb-20">
        <div className="flex flex-col lg:flex-row items-center gap-10 sm:gap-14 lg:gap-10">

          {/* ────────────────────────────────────────────────────── */}
          {/* Left: copy                                            */}
          {/* ────────────────────────────────────────────────────── */}
          <div className="flex-1 text-center lg:text-left w-full">

            {/* Brand mark */}
            <div className="flex mb-6 sm:mb-7 justify-center lg:justify-start">
              <Image
                src="/stax-logo.png"
                alt="STAX"
                width={240}
                height={240}
                priority
                className="w-[180px] h-[180px] sm:w-[200px] sm:h-[200px] lg:w-[170px] lg:h-[170px]"
                style={{ filter: "drop-shadow(0 12px 48px rgba(124,108,246,0.55))" }}
              />
            </div>

            {/* Primary headline */}
            <h1
              className="font-black tracking-tight mb-5 sm:mb-6"
              style={{
                letterSpacing: "-0.03em",
                lineHeight: 1.02,
              }}
            >
              <span
                className="block text-[44px] sm:text-[64px] lg:text-[80px]"
                style={{ color: "#F2F2F7" }}
              >
                Stack it.
              </span>
              <span
                className="block text-[44px] sm:text-[64px] lg:text-[80px]"
                style={{
                  background: "linear-gradient(120deg, #9B8FF7 0%, #7C6CF6 40%, #BF5AF2 100%)",
                  WebkitBackgroundClip: "text",
                  WebkitTextFillColor: "transparent",
                  backgroundClip: "text",
                }}
              >
                Snap it.
              </span>
              <span
                className="block text-[44px] sm:text-[64px] lg:text-[80px]"
                style={{ color: "#F2F2F7" }}
              >
                Track it.
              </span>
            </h1>

            {/* Supporting description */}
            <p
              className="text-[15px] sm:text-[17px] leading-relaxed sm:leading-loose mb-7 sm:mb-9 max-w-md mx-auto lg:mx-0"
              style={{ color: "#888890" }}
            >
              The poker companion for players who take their game seriously.
              Session tracking, AI chip scanning, and card room discovery —
              in one beautiful app.
            </p>

            {/* CTA row */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center lg:items-start gap-3 mb-6 sm:mb-7 max-w-sm mx-auto lg:mx-0 lg:max-w-none">
              <Link
                href="#download"
                className="btn btn-primary justify-center"
                style={{ padding: "14px 24px", borderRadius: "12px", fontSize: "0.9375rem" }}
              >
                <AppleIcon />
                <span className="text-left">
                  <span className="block text-[10px] font-normal leading-none mb-0.5 text-white/60">
                    Download on the
                  </span>
                  App Store
                </span>
              </Link>
              <Link
                href="#download"
                className="btn btn-ghost justify-center"
                style={{ padding: "14px 24px", borderRadius: "12px", fontSize: "0.9375rem" }}
              >
                <PlayStoreIcon />
                <span className="text-left">
                  <span className="block text-[10px] font-normal leading-none mb-0.5" style={{ color: "#4A4A52" }}>
                    Get it on
                  </span>
                  Google Play
                </span>
              </Link>
            </div>

            {/* Trust checklist */}
            <div className="flex items-center gap-x-5 gap-y-2 justify-center lg:justify-start flex-wrap">
              {[
                "Free to download",
                "7-day trial",
                "No credit card",
              ].map((text) => (
                <span
                  key={text}
                  className="flex items-center gap-1.5 text-[11px] sm:text-xs"
                  style={{ color: "#5C5C64" }}
                >
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#34C759" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  {text}
                </span>
              ))}
            </div>

            {/* QR + venue count — desktop only, mobile hides */}
            <div className="hidden lg:flex items-center gap-5 mt-10">
              <div className="flex items-center gap-3">
                <div className="rounded-xl p-1.5" style={{ background: "#F2F2F7" }}>
                  <QRCodeDisplay size={52} />
                </div>
                <div className="text-left">
                  <p className="text-xs font-semibold" style={{ color: "#F2F2F7" }}>
                    Scan to download
                  </p>
                  <p className="text-xs" style={{ color: "#4A4A52" }}>
                    iOS &amp; Android
                  </p>
                </div>
              </div>

              <div className="w-px h-8" style={{ background: "#1C1C21" }} />

              <div className="flex items-center gap-2 text-xs" style={{ color: "#5C5C64" }}>
                <span>420+ card rooms</span>
                <span style={{ color: "#2C2C34" }}>·</span>
                <span>38 states</span>
                <span style={{ color: "#2C2C34" }}>·</span>
                <span>$4.99/mo Premium</span>
              </div>
            </div>
          </div>

          {/* ────────────────────────────────────────────────────── */}
          {/* Right: phone + floating badges                        */}
          {/* ────────────────────────────────────────────────────── */}
          <div className="shrink-0 relative w-full flex justify-center lg:block lg:w-auto">
            {/* Mobile phone — smaller, centred */}
            <div className="lg:hidden flex justify-center float-animation">
              <PhoneMockup size={232} />
            </div>

            {/* Desktop phone with floating badges */}
            <div className="hidden lg:block relative" style={{ width: 340 }}>
              <div
                className="absolute badge-float"
                style={{ top: "8%", left: -8, zIndex: 20 }}
              >
                <FloatingBadge
                  variant="green"
                  icon={
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#34C759" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
                      <polyline points="17 6 23 6 23 12" />
                    </svg>
                  }
                  title="+$2,340"
                  subtitle="Session profit"
                />
              </div>

              <div
                className="absolute badge-float-2"
                style={{ bottom: "18%", right: -12, zIndex: 20 }}
              >
                <FloatingBadge
                  variant="purple"
                  icon={
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#7C6CF6" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                    </svg>
                  }
                  title="AI Chip Count"
                  subtitle="On-device · instant"
                />
              </div>

              <div className="flex justify-center float-animation">
                <PhoneMockup size={292} />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Floor fade */}
      <div
        className="absolute bottom-0 left-0 right-0 h-40 sm:h-56 pointer-events-none"
        style={{ background: "linear-gradient(to bottom, transparent, #070709)" }}
      />
    </section>
  );
}

/* ───────────────────────────────────────────────────────────── */
/* Reusable floating badge                                       */
/* ───────────────────────────────────────────────────────────── */
function FloatingBadge({
  variant,
  icon,
  title,
  subtitle,
}: {
  variant: "green" | "purple";
  icon: React.ReactNode;
  title: string;
  subtitle: string;
}) {
  const accent = variant === "green" ? "#34C759" : "#9B8FF7";
  const bg = variant === "green" ? "rgba(52,199,89,0.12)" : "rgba(124,108,246,0.12)";
  const border = variant === "green" ? "rgba(52,199,89,0.25)" : "rgba(124,108,246,0.25)";
  const shadowColor = variant === "green" ? "rgba(52,199,89,0.08)" : "rgba(124,108,246,0.08)";

  return (
    <div
      className="flex items-center gap-2.5 px-3.5 py-2.5 rounded-2xl"
      style={{
        background: "rgba(12,12,16,0.88)",
        border: `1px solid ${border}`,
        backdropFilter: "blur(16px)",
        WebkitBackdropFilter: "blur(16px)",
        boxShadow: `0 8px 32px rgba(0,0,0,0.5), 0 0 0 1px ${shadowColor}`,
      }}
    >
      <div
        className="w-7 h-7 rounded-lg flex items-center justify-center shrink-0"
        style={{ background: bg }}
      >
        {icon}
      </div>
      <div>
        <p className="text-[11px] font-black leading-tight" style={{ color: accent }}>
          {title}
        </p>
        <p className="text-[9px] leading-tight mt-0.5" style={{ color: "#636366" }}>
          {subtitle}
        </p>
      </div>
    </div>
  );
}

function AppleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
      <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z" />
    </svg>
  );
}

function PlayStoreIcon() {
  return (
    <svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor">
      <path d="M3 20.5v-17c0-.83.94-1.3 1.6-.8l14 8.5c.6.36.6 1.24 0 1.6l-14 8.5c-.66.5-1.6.03-1.6-.8z" />
    </svg>
  );
}
