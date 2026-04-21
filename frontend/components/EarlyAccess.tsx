"use client";

import { useState } from "react";
import { ArrowRight, CheckCircle2, Mail } from "lucide-react";

export default function EarlyAccess() {
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState<"idle" | "submitted">("idle");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (email.trim()) setStatus("submitted");
  };

  return (
    <section
      className="relative py-16 sm:py-24 overflow-hidden"
      style={{ background: "#0D0B14" }}
    >
      {/* Ambient radial */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          background:
            "radial-gradient(ellipse 70% 60% at 50% 40%, rgba(124,108,246,0.13) 0%, transparent 65%)",
        }}
      />

      <div className="absolute top-0 left-0 right-0 section-divider" />

      <div className="relative max-w-2xl mx-auto px-5 sm:px-6 text-center">

        {/* Chip decoration */}
        <div className="flex justify-center gap-2 sm:gap-2.5 mb-7 sm:mb-8" aria-hidden>
          {["#7C6CF6", "#34C759", "#FF9F0A", "#FF453A", "#5AC8FA"].map((color, i) => (
            <div key={i}>
              {[0, 1, 2].map((j) => (
                <div
                  key={j}
                  className="w-[22px] h-[7px] sm:w-[26px] sm:h-[8px]"
                  style={{
                    borderRadius: "50%",
                    background: color,
                    border: "1px solid rgba(255,255,255,0.1)",
                    marginBottom: 2,
                    opacity: 1 - j * 0.22,
                    boxShadow: j === 0 ? `0 4px 12px ${color}30` : "none",
                  }}
                />
              ))}
            </div>
          ))}
        </div>

        <span className="eyebrow mb-4 block">Launching Soon</span>

        <h2
          className="text-[32px] sm:text-5xl font-black tracking-tight mb-4 sm:mb-5"
          style={{
            color: "#F2F2F7",
            letterSpacing: "-0.02em",
            lineHeight: 1.05,
          }}
        >
          Get Early Access
        </h2>

        <p
          className="text-[15px] sm:text-base leading-relaxed sm:leading-loose mb-8 sm:mb-10 max-w-md mx-auto"
          style={{ color: "#8E8E94" }}
        >
          STAX is entering beta. Drop your email and be first at the table when
          we launch. Beta users get{" "}
          <span style={{ color: "#34C759", fontWeight: 600 }}>
            3 months of Premium free
          </span>
          .
        </p>

        {/* Form */}
        {status === "idle" ? (
          <form onSubmit={handleSubmit} className="relative max-w-md mx-auto">
            <div
              className="flex items-center gap-1 p-1.5 rounded-2xl"
              style={{
                background: "#0F0F14",
                border: "1px solid #24242C",
                boxShadow: "0 8px 32px rgba(0,0,0,0.4)",
              }}
            >
              <div
                className="pl-3 pr-1 shrink-0 hidden sm:block"
                style={{ color: "#4A4A52" }}
              >
                <Mail size={16} strokeWidth={2} />
              </div>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="your@email.com"
                required
                className="flex-1 min-w-0 bg-transparent px-3 sm:px-2 py-3 text-[14px] outline-none"
                style={{ color: "#F2F2F7" }}
              />
              <button
                type="submit"
                className="btn btn-primary shrink-0"
                style={{
                  padding: "10px 16px",
                  borderRadius: "12px",
                  fontSize: "0.8125rem",
                }}
              >
                <span className="hidden sm:inline">Join Waitlist</span>
                <span className="sm:hidden">Join</span>
                <ArrowRight size={14} strokeWidth={2.5} />
              </button>
            </div>
          </form>
        ) : (
          <div
            className="inline-flex items-center gap-3 px-5 py-4 rounded-2xl"
            style={{
              background: "rgba(52,199,89,0.08)",
              border: "1px solid rgba(52,199,89,0.25)",
              boxShadow: "0 8px 32px rgba(52,199,89,0.08)",
            }}
          >
            <CheckCircle2 size={22} style={{ color: "#34C759" }} />
            <div className="text-left">
              <div
                className="text-sm font-semibold"
                style={{ color: "#34C759" }}
              >
                You&apos;re on the list
              </div>
              <div className="text-xs mt-0.5" style={{ color: "#8E8E94" }}>
                We&apos;ll email you when beta opens.
              </div>
            </div>
          </div>
        )}

        <div className="flex items-center justify-center gap-3 mt-6">
          {[
            "No spam",
            "Unsubscribe anytime",
            "Your email stays private",
          ].map((item, i) => (
            <div key={item} className="flex items-center gap-2">
              {i > 0 && (
                <span className="w-1 h-1 rounded-full" style={{ background: "#2C2C34" }} />
              )}
              <span className="text-[11px]" style={{ color: "#4A4A52" }}>
                {item}
              </span>
            </div>
          ))}
        </div>
      </div>

      <div className="absolute bottom-0 left-0 right-0 section-divider" />
    </section>
  );
}
