import {
  LayoutGrid,
  Sparkles,
  TrendingUp,
  MapPin,
  BookOpen,
  Camera,
  ArrowRight,
} from "lucide-react";
import Link from "next/link";
import type { CSSProperties } from "react";

const heroFeatures = [
  {
    icon: Sparkles,
    title: "AI Stack Counter",
    description:
      "Point your camera at any multicolor stack and get an exact dollar value in seconds. Cloud-powered chip counting via OpenAI handles even the messiest casino setups.",
    accent: "#FF9F0A",
    tier: "Premium",
    tag: "The Killer Feature",
    wide: true,
  },
  {
    icon: LayoutGrid,
    title: "Session Tracker",
    description:
      "Create sessions for every game — pick your casino, stakes, and game type. Every session, every night, every casino, organized in one place.",
    accent: "#7C6CF6",
    tier: "Free",
    tag: "Core Feature",
    wide: false,
  },
];

const galleryFeature = {
  icon: Camera,
  title: "Photo Gallery",
  description:
    "Snap your stack in-app and build a gallery for every session. Full-screen viewer, swipe navigation, and 1–5 star ratings — so your biggest nights are always one tap away.",
  accent: "#BF5AF2",
  tier: "Free",
  tag: "Fan Favorite",
};

const supportingFeatures = [
  {
    icon: TrendingUp,
    title: "P&L Tracking",
    description:
      "Log buy-in, cash-out, and profit/loss per session. Running totals across every casino, automatically.",
    accent: "#34C759",
    tier: "Free",
  },
  {
    icon: MapPin,
    title: "Casino Finder",
    description:
      "380 venues across 38 states with one-tap directions. Set search radius and mark favorites.",
    accent: "#FF453A",
    tier: "Free",
  },
  {
    icon: BookOpen,
    title: "Hand History",
    description:
      "Record key hands with hole cards, board, position, and outcome. Your biggest pots, preserved.",
    accent: "#5AC8FA",
    tier: "Free",
  },
];

const tierStyles: Record<string, { bg: string; text: string; border: string }> =
  {
    Free: {
      bg: "rgba(90,200,250,0.08)",
      text: "#5AC8FA",
      border: "rgba(90,200,250,0.18)",
    },
    Premium: {
      bg: "rgba(124,108,246,0.12)",
      text: "#9B8FF7",
      border: "rgba(124,108,246,0.25)",
    },
  };

export default function Features() {
  return (
    <section
      id="features"
      className="py-20 sm:py-28 relative"
      style={{ background: "#0B0B0F" }}
    >
      <div className="absolute top-0 left-0 right-0 section-divider" />

      <div className="max-w-6xl mx-auto px-5 sm:px-6">
        {/* Section header */}
        <div className="text-center mb-14 sm:mb-20">
          <span className="eyebrow mb-4 block">Powered by AI</span>
          <h2
            className="text-[32px] sm:text-5xl font-black tracking-tight mb-4 sm:mb-5"
            style={{
              color: "#F2F2F7",
              letterSpacing: "-0.02em",
              lineHeight: 1.05,
            }}
          >
            Built different.
          </h2>
          <p
            className="max-w-lg mx-auto text-[15px] sm:text-base leading-relaxed sm:leading-loose"
            style={{ color: "#8E8E94" }}
          >
            Most poker apps track wins and losses. STAX counts your chips for
            you.
          </p>
        </div>

        {/* Hero AI row */}
        <div className="grid grid-cols-1 sm:grid-cols-5 gap-4 mb-4">
          {heroFeatures.map((feature) => {
            const Icon = feature.icon;
            const tierStyle = tierStyles[feature.tier];
            const cardStyle = {
              ["--accent" as string]: feature.accent,
            } as CSSProperties;

            return (
              <div
                key={feature.title}
                className={`card card-accent p-7 sm:p-8 flex flex-col group relative overflow-hidden ${
                  feature.wide ? "sm:col-span-3" : "sm:col-span-2"
                }`}
                style={cardStyle}
              >
                {/* Subtle radial glow behind card */}
                <div
                  className="absolute inset-0 pointer-events-none"
                  style={{
                    background: `radial-gradient(ellipse at 20% 20%, ${feature.accent}0A 0%, transparent 65%)`,
                  }}
                />

                {/* Top row: icon + tier */}
                <div className="flex items-start justify-between mb-6 relative">
                  <div className="flex items-center gap-3">
                    <div
                      className="relative w-12 h-12 rounded-xl flex items-center justify-center"
                      style={{
                        background: `${feature.accent}15`,
                        border: `1px solid ${feature.accent}28`,
                      }}
                    >
                      <div
                        className="absolute inset-0 rounded-xl opacity-0 transition-opacity group-hover:opacity-100"
                        style={{
                          background: `radial-gradient(circle at center, ${feature.accent}28 0%, transparent 70%)`,
                        }}
                      />
                      <Icon
                        size={22}
                        style={{ color: feature.accent, position: "relative" }}
                        strokeWidth={2}
                      />
                    </div>
                    <span
                      className="text-[10px] font-bold uppercase px-2.5 py-1 rounded-full"
                      style={{
                        color: feature.accent,
                        background: `${feature.accent}12`,
                        border: `1px solid ${feature.accent}28`,
                        letterSpacing: "0.12em",
                      }}
                    >
                      {feature.tag}
                    </span>
                  </div>
                  <span
                    className="shrink-0 text-[9px] font-bold uppercase px-2 py-1 rounded-full"
                    style={{
                      background: tierStyle.bg,
                      color: tierStyle.text,
                      border: `1px solid ${tierStyle.border}`,
                      letterSpacing: "0.1em",
                    }}
                  >
                    {feature.tier}
                  </span>
                </div>

                <h3
                  className="text-[19px] sm:text-[22px] font-black mb-3 leading-snug tracking-tight relative"
                  style={{ color: "#F2F2F7", letterSpacing: "-0.02em" }}
                >
                  {feature.title}
                </h3>

                <p
                  className="text-[13px] sm:text-[14px] leading-relaxed relative"
                  style={{ color: "#8E8E94" }}
                >
                  {feature.description}
                </p>
              </div>
            );
          })}
        </div>

        {/* Photo Gallery — full-width feature card */}
        {(() => {
          const Icon = galleryFeature.icon;
          const tierStyle = tierStyles[galleryFeature.tier];
          const cardStyle = {
            ["--accent" as string]: galleryFeature.accent,
          } as CSSProperties;
          return (
            <div
              className="card card-accent p-7 sm:p-8 flex flex-col sm:flex-row sm:items-center gap-6 group relative overflow-hidden mb-4"
              style={cardStyle}
            >
              <div
                className="absolute inset-0 pointer-events-none"
                style={{
                  background: `radial-gradient(ellipse at 80% 50%, ${galleryFeature.accent}08 0%, transparent 60%)`,
                }}
              />
              {/* Icon + tags */}
              <div className="flex items-center gap-4 shrink-0 relative">
                <div
                  className="relative w-12 h-12 rounded-xl flex items-center justify-center"
                  style={{
                    background: `${galleryFeature.accent}15`,
                    border: `1px solid ${galleryFeature.accent}28`,
                  }}
                >
                  <div
                    className="absolute inset-0 rounded-xl opacity-0 transition-opacity group-hover:opacity-100"
                    style={{
                      background: `radial-gradient(circle at center, ${galleryFeature.accent}28 0%, transparent 70%)`,
                    }}
                  />
                  <Icon
                    size={22}
                    style={{ color: galleryFeature.accent, position: "relative" }}
                    strokeWidth={2}
                  />
                </div>
                <span
                  className="text-[10px] font-bold uppercase px-2.5 py-1 rounded-full"
                  style={{
                    color: galleryFeature.accent,
                    background: `${galleryFeature.accent}12`,
                    border: `1px solid ${galleryFeature.accent}28`,
                    letterSpacing: "0.12em",
                  }}
                >
                  {galleryFeature.tag}
                </span>
                <span
                  className="text-[9px] font-bold uppercase px-2 py-1 rounded-full"
                  style={{
                    background: tierStyle.bg,
                    color: tierStyle.text,
                    border: `1px solid ${tierStyle.border}`,
                    letterSpacing: "0.1em",
                  }}
                >
                  {galleryFeature.tier}
                </span>
              </div>
              {/* Text */}
              <div className="relative">
                <h3
                  className="text-[19px] sm:text-[22px] font-black mb-2 leading-snug tracking-tight"
                  style={{ color: "#F2F2F7", letterSpacing: "-0.02em" }}
                >
                  {galleryFeature.title}
                </h3>
                <p
                  className="text-[13px] sm:text-[14px] leading-relaxed"
                  style={{ color: "#8E8E94" }}
                >
                  {galleryFeature.description}
                </p>
              </div>
            </div>
          );
        })()}

        {/* Supporting features row */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-12 sm:mb-14">
          {supportingFeatures.map((feature) => {
            const Icon = feature.icon;
            const tierStyle = tierStyles[feature.tier];
            const cardStyle = {
              ["--accent" as string]: feature.accent,
            } as CSSProperties;

            return (
              <div
                key={feature.title}
                className="card card-accent p-6 flex flex-col group"
                style={cardStyle}
              >
                <div className="flex items-start justify-between mb-5">
                  <div
                    className="relative w-11 h-11 rounded-xl flex items-center justify-center"
                    style={{
                      background: `${feature.accent}12`,
                      border: `1px solid ${feature.accent}22`,
                    }}
                  >
                    <div
                      className="absolute inset-0 rounded-xl opacity-0 transition-opacity group-hover:opacity-100"
                      style={{
                        background: `radial-gradient(circle at center, ${feature.accent}22 0%, transparent 70%)`,
                      }}
                    />
                    <Icon
                      size={19}
                      style={{ color: feature.accent, position: "relative" }}
                      strokeWidth={2.2}
                    />
                  </div>
                  <span
                    className="shrink-0 text-[9px] font-bold uppercase px-2 py-1 rounded-full"
                    style={{
                      background: tierStyle.bg,
                      color: tierStyle.text,
                      border: `1px solid ${tierStyle.border}`,
                      letterSpacing: "0.1em",
                    }}
                  >
                    {feature.tier}
                  </span>
                </div>

                <h3
                  className="text-[15px] font-bold mb-2 leading-snug tracking-tight"
                  style={{ color: "#F2F2F7" }}
                >
                  {feature.title}
                </h3>

                <p
                  className="text-[13px] leading-relaxed"
                  style={{ color: "#7A7A82" }}
                >
                  {feature.description}
                </p>
              </div>
            );
          })}
        </div>

        {/* CTA to full features page */}
        <div className="text-center">
          <Link
            href="/features"
            className="inline-flex items-center gap-2 text-sm font-semibold transition-all group"
            style={{ color: "#7C6CF6" }}
          >
            <span>See all 12 features</span>
            <ArrowRight
              size={15}
              strokeWidth={2.5}
              className="transition-transform group-hover:translate-x-0.5"
            />
          </Link>
        </div>
      </div>

      <div className="absolute bottom-0 left-0 right-0 section-divider" />
    </section>
  );
}
