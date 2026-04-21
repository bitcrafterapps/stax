import {
  Camera,
  LayoutGrid,
  Cpu,
  Sparkles,
  MapPin,
  Star,
  TrendingUp,
  BookOpen,
  Settings2,
  BarChart3,
  Spade,
  Share2,
} from "lucide-react";
import type { CSSProperties } from "react";

const features = [
  {
    icon: Camera,
    title: "Photo Gallery",
    description:
      "Capture chip stacks in-app and build a beautiful gallery per session. Full-screen viewer with swipe navigation.",
    accent: "#7C6CF6",
    tier: "Free",
    category: "Gallery & Sessions",
  },
  {
    icon: LayoutGrid,
    title: "Session Manager",
    description:
      "Create sessions with casino, game type (NLH, PLO, Omaha), and cash or tournament. All your poker history, organized.",
    accent: "#7C6CF6",
    tier: "Free",
    category: "Gallery & Sessions",
  },
  {
    icon: TrendingUp,
    title: "P&L Tracking",
    description:
      "Log buy-in, cash-out, and profit/loss for every session. See running totals across all casinos.",
    accent: "#34C759",
    tier: "Free",
    category: "Gallery & Sessions",
  },
  {
    icon: BookOpen,
    title: "Hand History",
    description:
      "Record key hands with hole cards, board, position, and outcome. Your biggest pots and biggest folds, preserved.",
    accent: "#5AC8FA",
    tier: "Free",
    category: "Gallery & Sessions",
  },
  {
    icon: Star,
    title: "Stack Ratings",
    description:
      "Rate chip stack photos 1–5 stars right in the gallery. Your best stacks rise to the top.",
    accent: "#FF9F0A",
    tier: "Free",
    category: "Gallery & Sessions",
  },
  {
    icon: Share2,
    title: "Stack Sharing",
    description:
      "Export your best chip stack photos. Premium users get clean exports — free users get a subtle \u2018via STAX\u2019 watermark.",
    accent: "#BF5AF2",
    tier: "Free / Premium",
    category: "Gallery & Sessions",
  },
  {
    icon: MapPin,
    title: "Casino Finder",
    description:
      "380 venues across 38 states — California, Washington, Nevada, Texas, Florida, and more. Set search radius, mark favorites, get one-tap directions.",
    accent: "#FF453A",
    tier: "Free",
    category: "Find & Explore",
  },
  {
    icon: Cpu,
    title: "Chip Scanner",
    description:
      "On-device AI (MediaPipe) detects chip denominations in real time from your camera — no internet required.",
    accent: "#34C759",
    tier: "Free",
    category: "AI & Scanning",
  },
  {
    icon: Sparkles,
    title: "AI Stack Counter",
    description:
      "Cloud-powered chip counting via OpenAI gives you exact dollar values for complex multicolor stacks.",
    accent: "#FF9F0A",
    tier: "Premium",
    category: "AI & Scanning",
  },
  {
    icon: Settings2,
    title: "Chip Configuration",
    description:
      "Configure exact chip denominations — color, value, count — per casino. Separate setups for cash and tournament.",
    accent: "#5AC8FA",
    tier: "Free / Premium",
    category: "AI & Scanning",
  },
  {
    icon: BarChart3,
    title: "Reports",
    description:
      "Filter your session history by date (All / 30D / 90D / YTD), venue, or game type. Win rate and P&L at a glance.",
    accent: "#7C6CF6",
    tier: "Free",
    category: "Reports & Training",
  },
  {
    icon: Spade,
    title: "Nutz Game",
    description:
      "Train your hand-reading. Identify the 1st and 2nd nuts on any board. Track your score and sharpen your edge.",
    accent: "#BF5AF2",
    tier: "Free",
    category: "Reports & Training",
  },
];

const categories = [
  "Gallery & Sessions",
  "Find & Explore",
  "AI & Scanning",
  "Reports & Training",
];

const categoryAccents: Record<string, string> = {
  "Gallery & Sessions": "#7C6CF6",
  "Find & Explore": "#FF453A",
  "AI & Scanning": "#34C759",
  "Reports & Training": "#5AC8FA",
};

const tierStyles: Record<string, { bg: string; text: string; border: string }> = {
  "Free": {
    bg: "rgba(90,200,250,0.08)",
    text: "#5AC8FA",
    border: "rgba(90,200,250,0.18)",
  },
  "Premium": {
    bg: "rgba(124,108,246,0.12)",
    text: "#9B8FF7",
    border: "rgba(124,108,246,0.25)",
  },
  "Free / Premium": {
    bg: "rgba(255,159,10,0.08)",
    text: "#FF9F0A",
    border: "rgba(255,159,10,0.18)",
  },
};

export default function Features() {
  return (
    <section id="features" className="py-20 sm:py-28 relative" style={{ background: "#0B0B0F" }}>
      <div className="absolute top-0 left-0 right-0 section-divider" />

      <div className="max-w-6xl mx-auto px-5 sm:px-6">
        {/* Section header */}
        <div className="text-center mb-14 sm:mb-20">
          <span className="eyebrow mb-4 block">Everything You Need</span>
          <h2
            className="text-[32px] sm:text-5xl font-black tracking-tight mb-4 sm:mb-5"
            style={{
              color: "#F2F2F7",
              letterSpacing: "-0.02em",
              lineHeight: 1.05,
            }}
          >
            Built for the felt
          </h2>
          <p
            className="max-w-lg mx-auto text-[15px] sm:text-base leading-relaxed sm:leading-loose"
            style={{ color: "#8E8E94" }}
          >
            Every feature was designed for serious poker players — from your
            first session photo to your deepest hand history.
          </p>
        </div>

        {/* Categories */}
        <div className="space-y-12 sm:space-y-16">
          {categories.map((category) => {
            const catFeatures = features.filter((f) => f.category === category);
            const accent = categoryAccents[category];

            return (
              <div key={category}>
                {/* Category label row */}
                <div className="flex items-center gap-3 sm:gap-4 mb-6 sm:mb-8">
                  <span
                    className="text-[10px] sm:text-[11px] font-bold uppercase px-2.5 sm:px-3 py-1 sm:py-1.5 rounded-full"
                    style={{
                      color: accent,
                      background: `${accent}0F`,
                      border: `1px solid ${accent}26`,
                      letterSpacing: "0.14em",
                    }}
                  >
                    {category}
                  </span>
                  <div className="flex-1 h-px" style={{ background: "#1C1C21" }} />
                  <span
                    className="text-[11px] sm:text-xs tabular-nums font-semibold"
                    style={{ color: "#4A4A52" }}
                  >
                    {String(catFeatures.length).padStart(2, "0")}
                  </span>
                </div>

                {/* Cards */}
                <div
                  className={`grid gap-4 ${
                    catFeatures.length === 1
                      ? "grid-cols-1 max-w-xl mx-auto"
                      : catFeatures.length === 2
                      ? "sm:grid-cols-2 max-w-3xl mx-auto"
                      : "sm:grid-cols-2 lg:grid-cols-3"
                  }`}
                >
                  {catFeatures.map((feature) => {
                    const Icon = feature.icon;
                    const tierStyle = tierStyles[feature.tier] ?? tierStyles["Free"];
                    const cardStyle = {
                      ["--accent" as string]: feature.accent,
                    } as CSSProperties;

                    return (
                      <div
                        key={feature.title}
                        className="card card-accent p-6 flex flex-col group"
                        style={cardStyle}
                      >
                        {/* Header row: icon + tier pill */}
                        <div className="flex items-start justify-between mb-5">
                          <div
                            className="relative w-11 h-11 rounded-xl flex items-center justify-center"
                            style={{
                              background: `${feature.accent}12`,
                              border: `1px solid ${feature.accent}22`,
                            }}
                          >
                            {/* Icon inner glow */}
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
              </div>
            );
          })}
        </div>

        {/* Legend */}
        <div className="mt-20 flex flex-wrap justify-center gap-6">
          {Object.entries(tierStyles).map(([label, style]) => (
            <div key={label} className="flex items-center gap-2">
              <span
                className="inline-block w-1.5 h-1.5 rounded-full"
                style={{ background: style.text }}
              />
              <span className="text-xs" style={{ color: "#6A6A72" }}>
                {label}
              </span>
            </div>
          ))}
        </div>
      </div>

      <div className="absolute bottom-0 left-0 right-0 section-divider" />
    </section>
  );
}
