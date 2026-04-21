const stats = [
  { value: "380", label: "Card Rooms", sublabel: "across 38 states", color: "#7C6CF6" },
  { value: "38", label: "States", sublabel: "CA · WA · NV · TX · FL & more", color: "#5AC8FA" },
  { value: "Free", label: "Download", sublabel: "no credit card needed", color: "#34C759" },
  { value: "7-Day", label: "Free Trial", sublabel: "Premium, on us", color: "#FF9F0A" },
];

const stateData = [
  { state: "California", abbr: "CA", count: 68 },
  { state: "Washington", abbr: "WA", count: 33 },
  { state: "Nevada", abbr: "NV", count: 26 },
  { state: "Florida", abbr: "FL", count: 26 },
  { state: "Texas", abbr: "TX", count: 22 },
  { state: "Montana", abbr: "MT", count: 21 },
  { state: "Oregon", abbr: "OR", count: 14 },
  { state: "Indiana", abbr: "IN", count: 12 },
  { state: "Mississippi", abbr: "MS", count: 12 },
  { state: "Arizona", abbr: "AZ", count: 11 },
  { state: "Oklahoma", abbr: "OK", count: 10 },
  { state: "Pennsylvania", abbr: "PA", count: 10 },
  { state: "Louisiana", abbr: "LA", count: 10 },
  { state: "Illinois", abbr: "IL", count: 10 },
  { state: "New Jersey", abbr: "NJ", count: 9 },
  { state: "New York", abbr: "NY", count: 9 },
  { state: "Minnesota", abbr: "MN", count: 8 },
  { state: "Michigan", abbr: "MI", count: 7 },
  { state: "South Dakota", abbr: "SD", count: 6 },
  { state: "Colorado", abbr: "CO", count: 5 },
  { state: "Ohio", abbr: "OH", count: 5 },
  { state: "New Mexico", abbr: "NM", count: 5 },
  { state: "Maryland", abbr: "MD", count: 4 },
  { state: "West Virginia", abbr: "WV", count: 4 },
  { state: "Iowa", abbr: "IA", count: 4 },
  { state: "Missouri", abbr: "MO", count: 4 },
  { state: "Wisconsin", abbr: "WI", count: 4 },
  { state: "Massachusetts", abbr: "MA", count: 3 },
  { state: "North Dakota", abbr: "ND", count: 3 },
  { state: "Virginia", abbr: "VA", count: 3 },
  { state: "Wyoming", abbr: "WY", count: 3 },
  { state: "Connecticut", abbr: "CT", count: 2 },
  { state: "North Carolina", abbr: "NC", count: 2 },
  { state: "Delaware", abbr: "DE", count: 1 },
  { state: "Kansas", abbr: "KS", count: 1 },
  { state: "Maine", abbr: "ME", count: 1 },
  { state: "Nebraska", abbr: "NE", count: 1 },
  { state: "Rhode Island", abbr: "RI", count: 1 },
];

const testimonials = [
  {
    quote:
      "Finally an app that gets what poker players actually want. My chip gallery is ridiculous now.",
    name: "Mike R.",
    detail: "Regular at Commerce Casino, LA",
    stars: 5,
    stack: "$3,200",
  },
  {
    quote:
      "The chip scanner is actual witchcraft. Pointed it at my stack and it counted $2,340 in about 2 seconds.",
    name: "Sarah K.",
    detail: "Tournament player, Las Vegas",
    stars: 5,
    stack: "$8,500",
  },
  {
    quote:
      "I've tried every poker app. STAX is the only one I actually open at the table. The casino finder alone is worth it.",
    name: "Dave T.",
    detail: "$2/$5 grinder, Los Angeles",
    stars: 5,
    stack: "$1,800",
  },
];

function Stars({ count }: { count: number }) {
  return (
    <div className="flex gap-0.5">
      {Array.from({ length: 5 }).map((_, i) => (
        <svg key={i} width="13" height="13" viewBox="0 0 24 24" fill={i < count ? "#FF9F0A" : "#2C2C34"}>
          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
        </svg>
      ))}
    </div>
  );
}

export default function SocialProof() {
  return (
    <section className="py-20 sm:py-28 relative" style={{ background: "#0A0A0C" }}>
      <div className="max-w-6xl mx-auto px-5 sm:px-6">

        {/* Stats strip */}
        <div
          className="grid grid-cols-2 md:grid-cols-4 gap-px mb-5 rounded-xl overflow-hidden"
          style={{ background: "#1C1C21", border: "1px solid #1F1F26" }}
        >
          {stats.map((stat) => (
            <div
              key={stat.label}
              className="relative flex flex-col items-center justify-center text-center py-7 sm:py-10 px-4 sm:px-6 group"
              style={{
                background: "linear-gradient(180deg, #121217 0%, #0E0E13 100%)",
              }}
            >
              {/* Top accent bar on hover */}
              <div
                className="absolute top-0 left-1/2 -translate-x-1/2 w-0 h-px group-hover:w-1/2 transition-all duration-500"
                style={{ background: stat.color, opacity: 0.6 }}
              />
              <div
                className="text-[28px] sm:text-4xl font-black mb-1.5 sm:mb-2 tabular-nums"
                style={{
                  color: stat.color,
                  letterSpacing: "-0.02em",
                }}
              >
                {stat.value}
              </div>
              <div
                className="text-[13px] font-semibold"
                style={{ color: "#F2F2F7" }}
              >
                {stat.label}
              </div>
              <div className="text-[11px] mt-1" style={{ color: "#5C5C64" }}>
                {stat.sublabel}
              </div>
            </div>
          ))}
        </div>

        {/* States coverage strip */}
        <div className="card flex flex-col sm:flex-row sm:items-center gap-4 px-5 sm:px-6 py-5 mb-14 sm:mb-20">
          <div className="shrink-0">
            <p
              className="text-[10px] font-bold uppercase mb-1"
              style={{ color: "#5C5C64", letterSpacing: "0.14em" }}
            >
              States Covered
            </p>
            <p
              className="text-xl font-black tabular-nums"
              style={{ color: "#7C6CF6", letterSpacing: "-0.01em" }}
            >
              38 States <span style={{ color: "#3A3A42", fontWeight: 400 }}>/</span> 380 Venues
            </p>
          </div>

          <div className="sm:w-px sm:h-12 h-px w-full" style={{ background: "#1F1F26" }} />

          <div className="flex flex-wrap gap-2">
            {stateData.map((s) => (
              <div
                key={s.abbr}
                className="flex items-center gap-2 px-3 py-1.5 rounded-lg transition-colors hover:border-stax-primary/30"
                style={{ background: "#0F0F14", border: "1px solid #1F1F26" }}
              >
                <span
                  className="text-[11px] font-black"
                  style={{ color: "#7C6CF6", letterSpacing: "0.02em" }}
                >
                  {s.abbr}
                </span>
                <span className="text-[11px] hidden sm:inline" style={{ color: "#8E8E94" }}>
                  {s.state}
                </span>
                <span
                  className="text-[10px] font-bold tabular-nums"
                  style={{ color: "#5C5C64" }}
                >
                  {s.count}
                </span>
              </div>
            ))}
          </div>

          <p
            className="sm:ml-auto shrink-0 text-[11px]"
            style={{ color: "#4A4A52" }}
          >
            More states coming soon
          </p>
        </div>

        {/* Testimonials header */}
        <div className="text-center mb-10 sm:mb-14">
          <span className="eyebrow mb-4 block">Early Access Players</span>
          <h2
            className="text-[32px] sm:text-5xl font-black tracking-tight"
            style={{
              color: "#F2F2F7",
              letterSpacing: "-0.02em",
              lineHeight: 1.05,
            }}
          >
            Players love their stacks.
          </h2>
        </div>

        {/* Testimonial cards */}
        <div className="grid md:grid-cols-3 gap-4 sm:gap-5">
          {testimonials.map((t) => (
            <div
              key={t.name}
              className="card p-7 flex flex-col relative"
            >
              {/* Decorative quote mark */}
              <div
                className="absolute top-5 right-6 font-black select-none pointer-events-none"
                style={{
                  fontSize: "64px",
                  lineHeight: 0.7,
                  color: "#7C6CF6",
                  opacity: 0.08,
                  fontFamily: "Georgia, serif",
                }}
              >
                &ldquo;
              </div>

              <Stars count={t.stars} />

              <p
                className="text-[14px] leading-loose mt-5 mb-6 flex-1"
                style={{ color: "#B8B8BE" }}
              >
                &ldquo;{t.quote}&rdquo;
              </p>

              <div
                className="flex items-center justify-between pt-4"
                style={{ borderTop: "1px solid #1F1F26" }}
              >
                <div className="flex items-center gap-2.5">
                  {/* Avatar initial */}
                  <div
                    className="w-9 h-9 rounded-full flex items-center justify-center text-[11px] font-black text-white shrink-0"
                    style={{
                      background: "linear-gradient(135deg, #7C6CF6, #5B4FD4)",
                    }}
                  >
                    {t.name.charAt(0)}
                  </div>
                  <div>
                    <div className="text-[13px] font-semibold" style={{ color: "#F2F2F7" }}>
                      {t.name}
                    </div>
                    <div className="text-[11px] mt-0.5" style={{ color: "#5C5C64" }}>
                      {t.detail}
                    </div>
                  </div>
                </div>
                <div
                  className="text-[10px] font-bold px-2 py-1 rounded-md tabular-nums"
                  style={{
                    background: "rgba(52,199,89,0.08)",
                    color: "#34C759",
                    border: "1px solid rgba(52,199,89,0.18)",
                    letterSpacing: "0.02em",
                  }}
                >
                  {t.stack}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
