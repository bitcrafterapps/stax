import Link from "next/link";
import { Check, Lock, Zap } from "lucide-react";

const freeFeatures = [
  { label: "Casino & Card Room Finder" },
  { label: "Photo Gallery", note: "10 photos / session" },
  { label: "Sessions", note: "3 active" },
  { label: "Chip Scanner (on-device)", note: "5 scans / day" },
  { label: "Favorite Venues", note: "3 max" },
  { label: "Chip Configs", note: "1 casino" },
];

const premiumFeatures = [
  { label: "Everything in Free" },
  { label: "Unlimited Sessions & Photos" },
  { label: "Unlimited Chip Scans" },
  { label: "All Casino Chip Configs" },
  { label: "Unlimited Favorites" },
  { label: "Watermark-Free Exports" },
  { label: "AI Stack Counter (OpenAI)", highlight: true },
];

export default function Pricing() {
  return (
    <section
      id="pricing"
      className="py-20 sm:py-28 relative"
      style={{ background: "#080809" }}
    >
      <div className="absolute top-0 left-0 right-0 section-divider" />

      <div className="max-w-5xl mx-auto px-5 sm:px-6">
        <div className="text-center mb-12 sm:mb-16">
          <span className="eyebrow mb-4 block">Pricing</span>
          <h2
            className="text-[32px] sm:text-5xl font-black tracking-tight mb-4 sm:mb-5"
            style={{
              color: "#F2F2F7",
              letterSpacing: "-0.02em",
              lineHeight: 1.05,
            }}
          >
            Start free.{" "}
            <span
              style={{
                background: "linear-gradient(120deg, #9B8FF7 0%, #BF5AF2 100%)",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
                backgroundClip: "text",
              }}
            >
              Unlock everything.
            </span>
          </h2>
          <p
            className="max-w-md mx-auto text-[15px] sm:text-base leading-relaxed sm:leading-loose"
            style={{ color: "#8E8E94" }}
          >
            STAX is free forever. Upgrade to Premium for unlimited sessions,
            AI chip counting, and clean exports.
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-4 sm:gap-5 max-w-3xl mx-auto">

          {/* Free */}
          <div
            className="card p-6 sm:p-8 flex flex-col"
            style={{ borderRadius: "16px" }}
          >
            <div className="mb-7">
              <p className="text-xs font-bold uppercase tracking-widest mb-3" style={{ color: "#636366" }}>
                Free
              </p>
              <div className="flex items-baseline gap-1.5 mb-1">
                <span className="text-5xl font-black text-stax-text">$0</span>
              </div>
              <p className="text-sm text-stax-text-dim">No credit card required</p>
            </div>

            <ul className="space-y-3.5 flex-1 mb-8">
              {freeFeatures.map((f) => (
                <li key={f.label} className="flex items-start gap-3">
                  <Check size={15} className="mt-0.5 shrink-0" style={{ color: "#5AC8FA" }} />
                  <span className="text-sm" style={{ color: "#AEAEB2" }}>
                    {f.label}
                    {f.note && (
                      <span className="ml-1.5 text-xs" style={{ color: "#4A4A52" }}>
                        ({f.note})
                      </span>
                    )}
                  </span>
                </li>
              ))}
              <li className="flex items-start gap-3 mt-4 pt-4" style={{ borderTop: "1px solid #2C2C34" }}>
                <Lock size={13} className="mt-0.5 shrink-0" style={{ color: "#3A3A42" }} />
                <span className="text-xs" style={{ color: "#3A3A42" }}>
                  AI Stack Counter — Premium only
                </span>
              </li>
            </ul>

            <Link
              href="#download"
              className="btn btn-ghost w-full justify-center"
              style={{ borderRadius: "12px" }}
            >
              Download Free
            </Link>
          </div>

          {/* Premium */}
          <div
            className="flex flex-col relative overflow-hidden"
            style={{
              borderRadius: "16px",
              background:
                "linear-gradient(145deg, #1A1628 0%, #16141F 40%, #121218 100%)",
              border: "1px solid rgba(124,108,246,0.35)",
              boxShadow:
                "0 0 0 1px rgba(124,108,246,0.06), 0 24px 64px rgba(124,108,246,0.1), 0 1px 0 rgba(255,255,255,0.04) inset",
            }}
          >
            {/* Ambient glow */}
            <div
              className="absolute -top-20 -right-20 w-64 h-64 rounded-full pointer-events-none"
              style={{
                background: "radial-gradient(circle, rgba(124,108,246,0.18) 0%, transparent 70%)",
                filter: "blur(20px)",
              }}
            />

            <div className="relative p-6 sm:p-8 flex flex-col h-full">
              {/* Badge */}
              <div className="absolute top-5 right-5 sm:top-6 sm:right-6">
                <span
                  className="flex items-center gap-1 text-[10px] font-black uppercase tracking-wider px-3 py-1.5 rounded-full text-white"
                  style={{ background: "linear-gradient(135deg, #7C6CF6, #5B4FD4)" }}
                >
                  <Zap size={9} fill="currentColor" />
                  Most Popular
                </span>
              </div>

              <div className="mb-7">
                <p
                  className="text-xs font-bold uppercase tracking-widest mb-3"
                  style={{ color: "#7C6CF6" }}
                >
                  Premium
                </p>

                <div className="flex items-baseline gap-1.5 mb-2">
                  <span className="text-5xl font-black text-stax-text">$4.99</span>
                  <span className="text-stax-text-muted text-sm mb-0.5">/ mo</span>
                </div>

                <div
                  className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold mb-3"
                  style={{ background: "rgba(52,199,89,0.1)", color: "#34C759", border: "1px solid rgba(52,199,89,0.2)" }}
                >
                  <span>or $39 / year</span>
                  <span
                    className="px-1.5 py-0.5 rounded-full text-[9px] font-black"
                    style={{ background: "#34C759", color: "#0A0A0C" }}
                  >
                    SAVE 35%
                  </span>
                </div>

                <p className="text-xs text-stax-text-dim">
                  7-day free trial · Cancel anytime
                </p>
              </div>

              <ul className="space-y-3.5 flex-1 mb-8">
                {premiumFeatures.map((f) => (
                  <li key={f.label} className="flex items-start gap-3">
                    <Check
                      size={15}
                      className="mt-0.5 shrink-0"
                      style={{ color: f.highlight ? "#7C6CF6" : "rgba(124,108,246,0.6)" }}
                    />
                    <span
                      className="text-sm"
                      style={{
                        color: f.highlight ? "#F2F2F7" : "#AEAEB2",
                        fontWeight: f.highlight ? 600 : 400,
                      }}
                    >
                      {f.label}
                    </span>
                  </li>
                ))}
              </ul>

              <div>
                <Link
                  href="#download"
                  className="btn btn-primary w-full justify-center mb-3"
                  style={{ borderRadius: "12px", padding: "14px 24px" }}
                >
                  Start 7-Day Free Trial
                </Link>
                <p className="text-center text-xs" style={{ color: "#636366" }}>
                  No credit card required to start
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom line */}
        <p className="text-center text-sm mt-10" style={{ color: "#4A4A52" }}>
          Less than a big blind per month. Unlimited chip flexing.
        </p>
      </div>

      {/* Bottom divider */}
      <div className="absolute bottom-0 left-0 right-0 section-divider" />
    </section>
  );
}
