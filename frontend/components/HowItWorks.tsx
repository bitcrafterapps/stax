import { PlusCircle, ScanLine, Trophy, Share2 } from "lucide-react";
import type { CSSProperties } from "react";

const steps = [
  {
    number: "01",
    icon: PlusCircle,
    title: "Create a Session",
    description:
      "Pick your casino, game type, and stakes. STAX organizes everything so you always know where you were and what you played.",
    accent: "#7C6CF6",
  },
  {
    number: "02",
    icon: ScanLine,
    title: "Capture Your Stack",
    description:
      "Use the in-app camera or AI chip scanner to photograph your stack. The chip counter gives you an instant dollar value.",
    accent: "#5AC8FA",
  },
  {
    number: "03",
    icon: Trophy,
    title: "Build Your Gallery",
    description:
      "Rate photos, swipe through sessions, and relive your biggest stacks. Your chip gallery is always ready to flex.",
    accent: "#34C759",
  },
  {
    number: "04",
    icon: Share2,
    title: "Share the Flex",
    description:
      "Send your best stacks to the group chat, drop them on Instagram, or text the table. Premium exports are watermark-free.",
    accent: "#FF9F0A",
    platforms: ["iMessage", "Instagram", "WhatsApp", "X"],
  },
];

export default function HowItWorks() {
  return (
    <section
      id="how-it-works"
      className="py-20 sm:py-28 relative"
      style={{ background: "#0D0D12" }}
    >
      <div className="absolute top-0 left-0 right-0 section-divider" />

      <div className="max-w-6xl mx-auto px-5 sm:px-6">
        <div className="text-center mb-12 sm:mb-20">
          <span className="eyebrow mb-4 block">Simple by Design</span>
          <h2
            className="text-[32px] sm:text-5xl font-black tracking-tight mb-4 sm:mb-5"
            style={{
              color: "#F2F2F7",
              letterSpacing: "-0.02em",
              lineHeight: 1.05,
            }}
          >
            Four steps. Zero friction.
          </h2>
          <p
            className="max-w-md mx-auto text-[15px] sm:text-base leading-relaxed sm:leading-loose"
            style={{ color: "#8E8E94" }}
          >
            From first hand to group chat in under a minute.
            STAX fits seamlessly into any session.
          </p>
        </div>

        {/* Steps */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 relative">
          {steps.map((step, index) => {
            const Icon = step.icon;
            const cardStyle = {
              ["--accent" as string]: step.accent,
            } as CSSProperties;

            return (
              <div
                key={step.title}
                className="card card-accent p-7 flex flex-col group"
                style={cardStyle}
              >
                {/* Step number watermark — top right */}
                <div
                  className="absolute top-4 right-5 text-[44px] font-black leading-none tabular-nums pointer-events-none"
                  style={{
                    color: `${step.accent}`,
                    opacity: 0.08,
                    letterSpacing: "-0.04em",
                  }}
                >
                  {step.number}
                </div>

                {/* Icon */}
                <div
                  className="relative w-12 h-12 rounded-xl flex items-center justify-center mb-5"
                  style={{
                    background: `${step.accent}12`,
                    border: `1px solid ${step.accent}22`,
                  }}
                >
                  <div
                    className="absolute inset-0 rounded-xl opacity-0 transition-opacity group-hover:opacity-100"
                    style={{
                      background: `radial-gradient(circle at center, ${step.accent}22 0%, transparent 70%)`,
                    }}
                  />
                  <Icon
                    size={20}
                    style={{ color: step.accent, position: "relative" }}
                    strokeWidth={2.2}
                  />
                </div>

                {/* Step label */}
                <p
                  className="text-[10px] font-bold uppercase mb-1.5"
                  style={{
                    color: `${step.accent}`,
                    letterSpacing: "0.14em",
                  }}
                >
                  Step {step.number}
                </p>

                <h3
                  className="text-[15px] font-bold mb-2.5 tracking-tight"
                  style={{ color: "#F2F2F7" }}
                >
                  {step.title}
                </h3>

                <p
                  className="text-[13px] leading-relaxed flex-1"
                  style={{ color: "#7A7A82" }}
                >
                  {step.description}
                </p>

                {/* Platform pills for last step */}
                {step.platforms && (
                  <div className="flex flex-wrap gap-1.5 mt-5">
                    {step.platforms.map((p) => (
                      <span
                        key={p}
                        className="text-[10px] font-semibold px-2 py-0.5 rounded-full"
                        style={{
                          background: "rgba(255,159,10,0.08)",
                          color: "#FF9F0A",
                          border: "1px solid rgba(255,159,10,0.18)",
                        }}
                      >
                        {p}
                      </span>
                    ))}
                  </div>
                )}

                {/* Connector arrow — visible between desktop cards only */}
                {index < steps.length - 1 && (
                  <div
                    className="hidden lg:flex absolute top-[44px] pointer-events-none items-center"
                    style={{ right: -18, zIndex: 10 }}
                  >
                    <svg width="24" height="10" viewBox="0 0 24 10" fill="none">
                      <circle
                        cx="2"
                        cy="5"
                        r="1.5"
                        fill={step.accent}
                        fillOpacity="0.35"
                      />
                      <circle
                        cx="9"
                        cy="5"
                        r="1.5"
                        fill={step.accent}
                        fillOpacity="0.25"
                      />
                      <path
                        d="M15 2 L20 5 L15 8"
                        stroke={steps[index + 1].accent}
                        strokeOpacity="0.5"
                        strokeWidth="1.5"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        fill="none"
                      />
                    </svg>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      <div className="absolute bottom-0 left-0 right-0 section-divider" />
    </section>
  );
}
