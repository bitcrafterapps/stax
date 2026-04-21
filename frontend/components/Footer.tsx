import Link from "next/link";
import StaxLogo from "./StaxLogo";

const footerLinks = [
  {
    label: "Features",
    links: [
      { label: "Photo Gallery", href: "#features" },
      { label: "Session Manager", href: "#features" },
      { label: "Chip Scanner", href: "#features" },
      { label: "AI Stack Counter", href: "#features" },
      { label: "Casino Finder", href: "#features" },
    ],
  },
  {
    label: "Product",
    links: [
      { label: "Screenshots", href: "/screenshots" },
      { label: "Pricing", href: "#pricing" },
      { label: "How It Works", href: "#how-it-works" },
      { label: "Download", href: "#download" },
    ],
  },
  {
    label: "Legal",
    links: [
      { label: "Privacy Policy", href: "/privacy" },
      { label: "Terms of Service", href: "/terms" },
    ],
  },
];

export default function Footer() {
  return (
    <footer
      className="relative pt-20 pb-10"
      style={{ background: "#070709" }}
    >
      <div className="absolute top-0 left-0 right-0 section-divider" />

      <div className="max-w-6xl mx-auto px-4 sm:px-6">
        <div className="grid grid-cols-2 md:grid-cols-5 gap-8 md:gap-10 mb-14">
          {/* Brand column */}
          <div className="col-span-2">
            <Link href="/" className="inline-flex items-center gap-2.5 mb-5 group">
              <StaxLogo size={30} />
              <span
                className="text-[1.1rem] font-black tracking-tight transition-colors"
                style={{ color: "#F2F2F7" }}
              >
                STAX
              </span>
            </Link>

            <p
              className="text-[13px] leading-loose mb-5 max-w-sm"
              style={{ color: "#7A7A82" }}
            >
              The ultimate poker session and chip tracking app. Built for
              players who take their game seriously.
            </p>

            <p
              className="text-[13px] font-semibold"
              style={{
                background: "linear-gradient(130deg, #9B8FF7 0%, #BF5AF2 100%)",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
                backgroundClip: "text",
                letterSpacing: "0.02em",
              }}
            >
              Stack it. Snap it. Track it.
            </p>
          </div>

          {/* Link columns */}
          {footerLinks.map((group) => (
            <div key={group.label}>
              <h4
                className="text-[10px] font-bold uppercase mb-5"
                style={{
                  color: "#5C5C64",
                  letterSpacing: "0.14em",
                }}
              >
                {group.label}
              </h4>
              <ul className="space-y-3">
                {group.links.map((link) => (
                  <li key={link.label}>
                    <Link
                      href={link.href}
                      className="footer-link text-[13px] transition-colors"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Bottom bar */}
        <div
          className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-8"
          style={{ borderTop: "1px solid #14141A" }}
        >
          <p className="text-[12px]" style={{ color: "#4A4A52" }}>
            &copy; 2026 Bitcraft Apps. All rights reserved.
          </p>
          <div className="flex items-center gap-5">
            <Link
              href="/privacy"
              className="text-[12px] transition-colors hover:text-stax-text"
              style={{ color: "#5C5C64" }}
            >
              Privacy
            </Link>
            <span className="w-1 h-1 rounded-full" style={{ background: "#2C2C34" }} />
            <Link
              href="/terms"
              className="text-[12px] transition-colors hover:text-stax-text"
              style={{ color: "#5C5C64" }}
            >
              Terms
            </Link>
            <span className="w-1 h-1 rounded-full" style={{ background: "#2C2C34" }} />
            <div className="flex items-center gap-1.5">
              <span
                className="w-1.5 h-1.5 rounded-full live-dot"
                style={{ background: "#34C759" }}
              />
              <span className="text-[12px]" style={{ color: "#5C5C64" }}>
                All systems operational
              </span>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
