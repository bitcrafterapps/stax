"use client";

import { useState, useCallback } from "react";
import Image from "next/image";
import { ChevronLeft, ChevronRight } from "lucide-react";

const slides = [
  {
    src: "/screenshots/855.jpg",
    label: "Splash Screen",
    desc: "A bold entrance every time you open the app",
    accent: "#7C6CF6",
  },
  {
    src: "/screenshots/857.jpg",
    label: "Photo Gallery",
    desc: "Real chip photos organized by casino — Commerce, Hawaiian Gardens, Wynn",
    accent: "#7C6CF6",
  },
  {
    src: "/screenshots/869.jpg",
    label: "Session Tracker",
    desc: "All sessions at a glance — 3 casinos, $4,150 total profit",
    accent: "#34C759",
  },
  {
    src: "/screenshots/865.jpg",
    label: "Session Details",
    desc: "+$1,735 at Commerce Casino · 200k Guaranteed · NLH Tournament",
    accent: "#34C759",
  },
  {
    src: "/screenshots/861.jpg",
    label: "Chip Stack Gallery",
    desc: "Rate and curate your best stacks — star ratings on every photo",
    accent: "#FF9F0A",
  },
  {
    src: "/screenshots/867.jpg",
    label: "Hand History",
    desc: "Log hole cards, board, position, and outcome for every key hand",
    accent: "#5AC8FA",
  },
  {
    src: "/screenshots/871.jpg",
    label: "Find Card Rooms",
    desc: "380 card rooms across 38 states — Near Me, By State, and Favorites tabs",
    accent: "#FF453A",
  },
  {
    src: "/screenshots/873.jpg",
    label: "Browse by State",
    desc: "Filter card rooms across California, Nevada, Texas, Florida, New York, and more",
    accent: "#FF453A",
  },
  {
    src: "/screenshots/875.jpg",
    label: "Chip Scanner",
    desc: "Live AI chip recognition — on-device or cloud via OpenAI",
    accent: "#BF5AF2",
  },
  {
    src: "/screenshots/879.jpg",
    label: "Chip Config — Cash",
    desc: "Set exact denominations for every casino's cash game chips",
    accent: "#FF9F0A",
  },
  {
    src: "/screenshots/881.jpg",
    label: "Chip Config — Tourney",
    desc: "Separate tournament chip configs, per venue",
    accent: "#FF9F0A",
  },
  {
    src: "/screenshots/883.jpg",
    label: "Reports",
    desc: "Filter by All / 30D / 90D / YTD · Win rate, totals, game type breakdowns",
    accent: "#5AC8FA",
  },
  {
    src: "/screenshots/885.jpg",
    label: "Nutz Game",
    desc: "Train your reads — identify the 1st and 2nd nuts on any board",
    accent: "#BF5AF2",
  },
  {
    src: "/screenshots/877.jpg",
    label: "About",
    desc: "Stack it. Snap it. Track it. — v1.0",
    accent: "#7C6CF6",
  },
];

// How many slides are visible on each side of the active one
const SIDE_COUNT = 2;

function getScale(offset: number): number {
  if (offset === 0) return 1;
  if (Math.abs(offset) === 1) return 0.78;
  if (Math.abs(offset) === 2) return 0.6;
  return 0.45;
}

function getZ(offset: number): number {
  return 100 - Math.abs(offset) * 20;
}

function getTranslateX(offset: number): number {
  if (offset === 0) return 0;
  const sign = offset > 0 ? 1 : -1;
  const abs = Math.abs(offset);
  if (abs === 1) return sign * 52;
  if (abs === 2) return sign * 88;
  return sign * 112;
}

function getOpacity(offset: number): number {
  if (Math.abs(offset) > SIDE_COUNT) return 0;
  if (Math.abs(offset) === 0) return 1;
  if (Math.abs(offset) === 1) return 0.8;
  return 0.5;
}

export default function ScreenshotCarousel() {
  const [active, setActive] = useState(0);

  const prev = useCallback(
    () => setActive((a) => Math.max(0, a - 1)),
    []
  );
  const next = useCallback(
    () => setActive((a) => Math.min(slides.length - 1, a + 1)),
    []
  );

  // Keyboard navigation
  const handleKey = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "ArrowLeft") prev();
      if (e.key === "ArrowRight") next();
    },
    [prev, next]
  );

  return (
    <section
      id="screenshots"
      className="py-20 sm:py-24 relative overflow-hidden"
      style={{ background: "#0D0D12" }}
      onKeyDown={handleKey}
      tabIndex={0}
      aria-label="App screenshots gallery"
    >
      <div className="absolute top-0 left-0 right-0 section-divider" />

      {/* Ambient glow under center slide */}
      <div
        className="absolute pointer-events-none"
        style={{
          bottom: "20%",
          left: "50%",
          transform: "translateX(-50%)",
          width: 500,
          height: 300,
          background:
            "radial-gradient(ellipse, rgba(124,108,246,0.12) 0%, transparent 70%)",
          filter: "blur(20px)",
        }}
      />

      <div className="relative max-w-6xl mx-auto px-5 sm:px-6">
        {/* Header */}
        <div className="text-center mb-12 sm:mb-16">
          <span className="eyebrow mb-4 block">Real Screenshots</span>
          <h2
            className="text-[32px] sm:text-5xl font-black tracking-tight mb-4 sm:mb-5"
            style={{
              color: "#F2F2F7",
              letterSpacing: "-0.02em",
              lineHeight: 1.05,
            }}
          >
            See it in action
          </h2>
          <p
            className="max-w-lg mx-auto text-[15px] sm:text-base leading-relaxed sm:leading-loose"
            style={{ color: "#8E8E94" }}
          >
            Every screen built for poker players. Dark, fast, and focused on
            what matters.
          </p>
        </div>

        {/* Cover flow stage */}
        <div
          className="relative select-none h-[440px] sm:h-[560px]"
        >
          {slides.map((slide, i) => {
            const offset = i - active;
            const absOffset = Math.abs(offset);
            if (absOffset > SIDE_COUNT + 1) return null;

            const scale = getScale(offset);
            const tx = getTranslateX(offset);
            const z = getZ(offset);
            const opacity = getOpacity(offset);
            const isActive = offset === 0;

            return (
              <div
                key={slide.src}
                onClick={() => {
                  if (!isActive) setActive(i);
                }}
                className="carousel-slide"
                style={{
                  position: "absolute",
                  top: "50%",
                  left: "50%",
                  zIndex: z,
                  transform: `translateX(${tx}%) scale(${scale})`,
                  opacity,
                  transition:
                    "transform 0.45s cubic-bezier(0.25,0.46,0.45,0.94), opacity 0.35s ease",
                  cursor: isActive ? "default" : "pointer",
                  transformOrigin: "center center",
                }}
              >
                {/* Screenshot — no phone frame */}
                <div
                  style={{
                    borderRadius: 14,
                    overflow: "hidden",
                    border: isActive
                      ? `1.5px solid rgba(124,108,246,0.55)`
                      : "1px solid rgba(58,58,66,0.4)",
                    boxShadow: isActive
                      ? "0 40px 100px rgba(0,0,0,0.8), 0 0 0 1px rgba(124,108,246,0.18), 0 0 40px rgba(124,108,246,0.2)"
                      : "0 14px 40px rgba(0,0,0,0.55)",
                    transition: "border-color 0.4s, box-shadow 0.4s",
                  }}
                >
                  <Image
                    src={slide.src}
                    alt={slide.label}
                    width={473}
                    height={1006}
                    style={{ width: "100%", height: "auto", display: "block" }}
                    priority={absOffset <= 1}
                    loading={absOffset <= 1 ? "eager" : "lazy"}
                  />
                </div>

                {/* Reflection */}
                {isActive && (
                  <div
                    style={{
                      position: "absolute",
                      bottom: -60,
                      left: 0,
                      right: 0,
                      height: 60,
                      background:
                        "linear-gradient(to bottom, rgba(124,108,246,0.08) 0%, transparent 100%)",
                      transform: "scaleY(-1)",
                      borderRadius: "0 0 14px 14px",
                      opacity: 0.4,
                      pointerEvents: "none",
                    }}
                  />
                )}
              </div>
            );
          })}
        </div>

        {/* Caption */}
        <div className="text-center mt-6 min-h-[56px]">
          <div
            className="text-[15px] font-bold mb-1.5 transition-colors duration-300 tracking-tight"
            style={{ color: slides[active].accent }}
          >
            {slides[active].label}
          </div>
          <p
            className="text-[13px] max-w-sm mx-auto leading-relaxed"
            style={{ color: "#7A7A82" }}
          >
            {slides[active].desc}
          </p>
        </div>

        {/* Controls */}
        <div className="flex items-center justify-center gap-3 mt-10">
          <button
            onClick={prev}
            disabled={active === 0}
            className="w-11 h-11 rounded-full flex items-center justify-center transition-all duration-200 hover:scale-105 active:scale-95"
            style={{
              background: "#13131A",
              border: "1px solid #24242C",
              opacity: active === 0 ? 0.3 : 1,
              cursor: active === 0 ? "not-allowed" : "pointer",
            }}
            aria-label="Previous"
          >
            <ChevronLeft size={17} color="#AEAEB2" strokeWidth={2.5} />
          </button>

          {/* Dots */}
          <div
            className="flex items-center gap-1.5 px-4 py-2 rounded-full"
            style={{ background: "#0F0F14", border: "1px solid #1C1C21" }}
          >
            {slides.map((_, i) => (
              <button
                key={i}
                onClick={() => setActive(i)}
                className="rounded-full transition-all duration-300"
                style={{
                  width: i === active ? 22 : 5,
                  height: 5,
                  background: i === active ? "#7C6CF6" : "#2C2C34",
                }}
                aria-label={`Screenshot ${i + 1}`}
              />
            ))}
          </div>

          <button
            onClick={next}
            disabled={active === slides.length - 1}
            className="w-11 h-11 rounded-full flex items-center justify-center transition-all duration-200 hover:scale-105 active:scale-95"
            style={{
              background:
                active === slides.length - 1
                  ? "#13131A"
                  : "linear-gradient(135deg, #7C6CF6, #5B4FD4)",
              border:
                active === slides.length - 1
                  ? "1px solid #24242C"
                  : "1px solid rgba(124,108,246,0.4)",
              boxShadow:
                active === slides.length - 1
                  ? "none"
                  : "0 6px 20px rgba(124,108,246,0.3)",
              opacity: active === slides.length - 1 ? 0.3 : 1,
              cursor: active === slides.length - 1 ? "not-allowed" : "pointer",
            }}
            aria-label="Next"
          >
            <ChevronRight size={17} color="#FFFFFF" strokeWidth={2.5} />
          </button>
        </div>

        {/* Counter + view all */}
        <div className="flex items-center justify-center gap-5 mt-6">
          <span className="text-[11px] tabular-nums font-semibold" style={{ color: "#5C5C64" }}>
            {String(active + 1).padStart(2, "0")} / {String(slides.length).padStart(2, "0")}
          </span>
          <div className="w-px h-3" style={{ background: "#1C1C21" }} />
          <a
            href="/screenshots"
            className="text-[11px] font-semibold nav-link"
            style={{ color: "#7C6CF6" }}
          >
            View all screenshots &rarr;
          </a>
        </div>
      </div>

      <div className="absolute bottom-0 left-0 right-0 section-divider" />
    </section>
  );
}
