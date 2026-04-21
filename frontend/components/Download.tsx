import Link from "next/link";
import Image from "next/image";
import QRCodeDisplay from "./QRCodeDisplay";

export default function Download() {
  return (
    <section
      id="download"
      className="relative py-20 sm:py-32 overflow-hidden"
      style={{ background: "#0A0A0C" }}
    >
      {/* Full-bleed purple radial */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          background:
            "radial-gradient(ellipse 90% 70% at 50% 50%, rgba(124,108,246,0.11) 0%, rgba(91,79,212,0.05) 50%, transparent 80%)",
        }}
      />

      {/* Top divider */}
      <div className="absolute top-0 left-0 right-0 section-divider" />

      {/* Grid subtle */}
      <div className="absolute inset-0 stax-grid-bg pointer-events-none" style={{ opacity: 0.4 }} />

      <div className="relative max-w-4xl mx-auto px-5 sm:px-6 text-center">

        {/* Logo centrepiece */}
        <div className="flex justify-center mb-7 sm:mb-8">
          <div className="relative">
            <div
              className="absolute inset-0 rounded-full"
              style={{
                background: "radial-gradient(circle, rgba(124,108,246,0.3) 0%, transparent 65%)",
                filter: "blur(24px)",
                transform: "scale(1.5)",
              }}
            />
            <Image
              src="/stax-logo.png"
              alt="STAX"
              width={96}
              height={96}
              className="w-20 h-20 sm:w-24 sm:h-24"
              style={{ filter: "drop-shadow(0 0 32px rgba(124,108,246,0.6))", position: "relative" }}
            />
          </div>
        </div>

        <span className="eyebrow mb-4 sm:mb-5 block">Ready to Play?</span>

        <h2
          className="text-[32px] sm:text-6xl font-black tracking-tight mb-5 sm:mb-6"
          style={{
            color: "#F2F2F7",
            letterSpacing: "-0.02em",
            lineHeight: 1.05,
          }}
        >
          Download STAX.
          <br />
          <span
            style={{
              background: "linear-gradient(130deg, #7C6CF6 0%, #BF5AF2 100%)",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
              backgroundClip: "text",
            }}
          >
            Free. Forever.
          </span>
        </h2>

        <p className="text-[15px] sm:text-lg leading-relaxed sm:leading-loose mb-10 sm:mb-12 max-w-lg mx-auto" style={{ color: "#8E8E94" }}>
          Join poker players who never lose track of a session.
          Stack it, snap it, track it — starting today.
        </p>

        {/* Download buttons */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-center gap-3 sm:gap-4 mb-10 sm:mb-14 max-w-sm mx-auto sm:max-w-none">
          <Link
            href="https://apps.apple.com"
            target="_blank"
            rel="noopener noreferrer"
            className="btn btn-ghost justify-center"
            style={{ padding: "14px 24px", borderRadius: "12px", fontSize: "0.9375rem" }}
          >
            <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
              <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.8-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z" />
            </svg>
            <span>
              <span className="block text-[11px] font-normal leading-none mb-0.5" style={{ color: "#636366" }}>
                Download on the
              </span>
              App Store
            </span>
          </Link>

          <Link
            href="https://play.google.com"
            target="_blank"
            rel="noopener noreferrer"
            className="btn btn-primary justify-center"
            style={{ padding: "14px 24px", borderRadius: "12px", fontSize: "0.9375rem" }}
          >
            <svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
              <path d="M3 20.5v-17c0-.83.94-1.3 1.6-.8l14 8.5c.6.36.6 1.24 0 1.6l-14 8.5c-.66.5-1.6.03-1.6-.8z" />
            </svg>
            <span>
              <span className="block text-[11px] font-normal leading-none mb-0.5 text-white/60">
                Get it on
              </span>
              Google Play
            </span>
          </Link>
        </div>

        {/* QR panel */}
        <div
          className="inline-flex flex-col sm:flex-row items-center gap-6 px-8 py-6 rounded-2xl mx-auto"
          style={{
            background: "rgba(28,28,33,0.8)",
            border: "1px solid #2C2C34",
            backdropFilter: "blur(12px)",
          }}
        >
          <div
            className="rounded-xl p-3"
            style={{ background: "#F2F2F7" }}
          >
            <QRCodeDisplay size={88} />
          </div>
          <div className="text-left">
            <p className="text-stax-text font-semibold mb-1">Scan to download</p>
            <p className="text-stax-text-muted text-sm leading-relaxed">
              Point your phone camera at the QR code
              <br />
              to go straight to the download page.
            </p>
            <p className="text-stax-text-dim text-xs mt-2">Works on iOS and Android</p>
          </div>
        </div>

        <p className="text-stax-text-dim text-xs mt-8">
          Download link coming soon — entering beta testing Q2 2026
        </p>
      </div>
    </section>
  );
}
