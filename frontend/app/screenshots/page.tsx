import type { Metadata } from "next";
import Image from "next/image";
import Link from "next/link";
import StaxLogo from "@/components/StaxLogo";

export const metadata: Metadata = {
  title: "Screenshots — STAX",
  description:
    "See STAX in action. Real screenshots of the poker session tracker, chip scanner, casino finder, and more.",
};

const groups = [
  {
    title: "Gallery & Sessions",
    description: "Organize chip stacks into beautiful session folders by casino.",
    screenshots: [
      {
        src: "/screenshots/855.jpg",
        caption: "Splash Screen",
        sub: "Clean entrance on every launch",
      },
      {
        src: "/screenshots/857.jpg",
        caption: "Casino / Card Rooms",
        sub: "Sessions grouped by venue with real chip photos",
      },
      {
        src: "/screenshots/859.jpg",
        caption: "Session Folder",
        sub: "P&L badge right on the folder",
      },
      {
        src: "/screenshots/861.jpg",
        caption: "Photo Gallery",
        sub: "Rate each chip stack 1–5 stars",
      },
    ],
  },
  {
    title: "Session Details & Tracking",
    description:
      "Log every buy-in, cash-out, and hand. Know your win rate at a glance.",
    screenshots: [
      {
        src: "/screenshots/865.jpg",
        caption: "Session Details",
        sub: "+$1,735 profit — Commerce Casino 200k GTD",
      },
      {
        src: "/screenshots/867.jpg",
        caption: "Hand History",
        sub: "Log key hands with hole cards and outcome",
      },
      {
        src: "/screenshots/869.jpg",
        caption: "Sessions List",
        sub: "Overall P&L across all casinos",
      },
      {
        src: "/screenshots/883.jpg",
        caption: "Reports",
        sub: "Filter by date range, venue, game type",
      },
    ],
  },
  {
    title: "Find & Scan",
    description:
      "380 card rooms across 38 states — California, Washington, Nevada, Texas, Florida, and more. Count your stack in seconds.",
    screenshots: [
      {
        src: "/screenshots/871.jpg",
        caption: "Find Card Rooms",
        sub: "380 card rooms across 38 states, one-tap directions",
      },
      {
        src: "/screenshots/873.jpg",
        caption: "By State",
        sub: "38 states — CA · WA · NV · TX · FL & more",
      },
      {
        src: "/screenshots/875.jpg",
        caption: "Chip Scanner",
        sub: "AI chip recognition — on-device or OpenAI",
      },
    ],
  },
  {
    title: "Configuration & More",
    description: "Fine-tune chip values for each casino and practice your reads.",
    screenshots: [
      {
        src: "/screenshots/879.jpg",
        caption: "Chip Config — Cash",
        sub: "Every denomination for every casino",
      },
      {
        src: "/screenshots/881.jpg",
        caption: "Chip Config — Tourney",
        sub: "Tournament chip sets configured separately",
      },
      {
        src: "/screenshots/877.jpg",
        caption: "About",
        sub: "Stack it. Snap it. Track it.",
      },
      {
        src: "/screenshots/885.jpg",
        caption: "Nutz Game",
        sub: "Train your hand-reading — find the nuts",
      },
    ],
  },
];

export default function ScreenshotsPage() {
  return (
    <div className="min-h-screen" style={{ background: "#0A0A0C" }}>
      {/* Header */}
      <header
        className="sticky top-0 z-50 border-b backdrop-blur-xl"
        style={{
          background: "rgba(10,10,12,0.9)",
          borderColor: "#1C1C21",
        }}
      >
        <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2">
            <StaxLogo size={32} />
            <span className="text-lg font-black text-stax-text">STAX</span>
          </Link>
          <div className="flex items-center gap-4">
            <Link
              href="/#download"
              className="hidden sm:inline-flex px-4 py-2 rounded-xl text-sm font-semibold text-white transition-opacity hover:opacity-90"
              style={{ background: "linear-gradient(135deg, #7C6CF6, #5B4FD4)" }}
            >
              Get the App
            </Link>
            <Link
              href="/"
              className="text-sm font-medium text-stax-text-muted hover:text-stax-text transition-colors"
            >
              ← Back
            </Link>
          </div>
        </div>
      </header>

      {/* Hero */}
      <div
        className="relative py-16 text-center overflow-hidden"
        style={{
          background:
            "linear-gradient(180deg, #0F0D18 0%, #0A0A0C 100%)",
          borderBottom: "1px solid #1C1C21",
        }}
      >
        <div
          className="absolute inset-0 pointer-events-none"
          style={{
            background:
              "radial-gradient(ellipse 60% 60% at 50% 50%, rgba(124,108,246,0.08) 0%, transparent 70%)",
          }}
        />
        <div className="relative max-w-2xl mx-auto px-4">
          <p
            className="text-sm font-semibold uppercase tracking-widest mb-3"
            style={{ color: "#7C6CF6", letterSpacing: "0.12em" }}
          >
            App Screenshots
          </p>
          <h1 className="text-3xl sm:text-4xl font-black text-stax-text mb-3">
            See every screen
          </h1>
          <p className="text-stax-text-muted text-base leading-relaxed">
            Real screenshots from STAX. Dark, fast, built for poker players.
          </p>
        </div>
      </div>

      {/* Screenshot groups */}
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-16 space-y-20">
        {groups.map((group) => (
          <div key={group.title}>
            {/* Group header */}
            <div className="mb-8">
              <h2 className="text-xl font-black text-stax-text mb-1">
                {group.title}
              </h2>
              <p className="text-sm text-stax-text-muted">{group.description}</p>
              <div
                className="mt-3 h-px w-16"
                style={{ background: "#7C6CF6" }}
              />
            </div>

            {/* Screenshot grid */}
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
              {group.screenshots.map((shot) => (
                <ScreenshotCard key={shot.src} {...shot} />
              ))}
            </div>
          </div>
        ))}
      </main>

      {/* Download CTA */}
      <div
        className="py-16 text-center"
        style={{ borderTop: "1px solid #1C1C21" }}
      >
        <p className="text-stax-text-muted text-sm mb-6">
          Ready to build your stack gallery?
        </p>
        <Link
          href="/#download"
          className="inline-flex items-center gap-2 px-6 py-3.5 rounded-xl text-sm font-bold text-white transition-opacity hover:opacity-90"
          style={{ background: "linear-gradient(135deg, #7C6CF6, #5B4FD4)" }}
        >
          Download STAX — Free
        </Link>
      </div>
    </div>
  );
}

function ScreenshotCard({
  src,
  caption,
  sub,
}: {
  src: string;
  caption: string;
  sub: string;
}) {
  return (
    <div className="group">
      {/* Phone frame */}
      <div
        className="relative overflow-hidden transition-all duration-200 group-hover:scale-[1.02]"
        style={{
          borderRadius: 28,
          border: "1.5px solid #2C2C34",
          background: "#000",
        }}
      >
        {/* Notch */}
        <div
          style={{
            position: "absolute",
            top: 0,
            left: "50%",
            transform: "translateX(-50%)",
            width: "32%",
            height: 14,
            background: "#000",
            borderRadius: "0 0 10px 10px",
            zIndex: 10,
          }}
        />

        <Image
          src={src}
          alt={caption}
          width={473}
          height={1006}
          style={{ width: "100%", height: "auto", display: "block" }}
          loading="lazy"
        />

        {/* Hover overlay */}
        <div
          className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex items-end"
          style={{
            background:
              "linear-gradient(to top, rgba(124,108,246,0.2) 0%, transparent 50%)",
          }}
        />
      </div>

      {/* Caption */}
      <div className="mt-3 px-1">
        <div className="text-xs font-bold text-stax-text">{caption}</div>
        <div className="text-xs text-stax-text-muted mt-0.5">{sub}</div>
      </div>
    </div>
  );
}
