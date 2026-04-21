const showcaseItems = [
  {
    title: "Session Gallery",
    description:
      "Every casino session becomes a beautifully organized photo album with game details, timestamps, and chip ratings.",
    accent: "#7C6CF6",
    screen: "gallery",
  },
  {
    title: "AI Chip Scanner",
    description:
      "Point the camera at any stack. On-device AI identifies each denomination instantly — no internet required.",
    accent: "#34C759",
    screen: "scanner",
  },
  {
    title: "Casino Finder",
    description:
      "Discover card rooms across the US. Filter by state, view game offerings, and get instant directions.",
    accent: "#5AC8FA",
    screen: "finder",
  },
];

function GalleryScreen() {
  const photos = [
    { stack: "$2,450", casino: "Bellagio", chips: ["#7C6CF6", "#34C759", "#FF453A"], stars: 4 },
    { stack: "$5,125", casino: "Wynn", chips: ["#FF9F0A", "#7C6CF6", "#34C759"], stars: 5 },
    { stack: "$890", casino: "Aria", chips: ["#34C759", "#FF453A", "#5AC8FA"], stars: 3 },
    { stack: "$1,600", casino: "MGM", chips: ["#7C6CF6", "#FF453A", "#5AC8FA"], stars: 4 },
  ];

  return (
    <div style={{ padding: "12px 10px 60px" }}>
      <div style={{ marginBottom: 10 }}>
        <div style={{ fontSize: 12, fontWeight: 800, color: "#F2F2F7", marginBottom: 2 }}>
          My Sessions
        </div>
        <div style={{ fontSize: 8, color: "#8E8E93" }}>4 sessions · 47 photos</div>
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 5 }}>
        {photos.map((p, i) => (
          <div
            key={i}
            style={{
              borderRadius: 12,
              background: "#1C1C21",
              border: "1px solid #2C2C34",
              padding: 8,
              height: 85,
              display: "flex",
              flexDirection: "column",
              justifyContent: "space-between",
            }}
          >
            {/* Chip stacks */}
            <div style={{ display: "flex", gap: 3 }}>
              {p.chips.map((color, j) => (
                <div key={j} style={{ display: "flex", flexDirection: "column", gap: 1 }}>
                  {[...Array(4)].map((_, k) => (
                    <div
                      key={k}
                      style={{
                        width: 14,
                        height: 5,
                        borderRadius: "50%",
                        background: color,
                        border: "1px solid rgba(255,255,255,0.1)",
                      }}
                    />
                  ))}
                </div>
              ))}
            </div>
            <div>
              <div style={{ fontSize: 9, fontWeight: 700, color: "#F2F2F7" }}>{p.stack}</div>
              <div style={{ fontSize: 7, color: "#8E8E93", marginBottom: 3 }}>{p.casino}</div>
              <div style={{ display: "flex", gap: 1 }}>
                {[...Array(5)].map((_, k) => (
                  <div
                    key={k}
                    style={{
                      width: 5,
                      height: 5,
                      background: k < p.stars ? "#FF9F0A" : "#3A3A42",
                      clipPath: "polygon(50% 0%, 61% 35%, 98% 35%, 68% 57%, 79% 91%, 50% 70%, 21% 91%, 32% 57%, 2% 35%, 39% 35%)",
                    }}
                  />
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function ScannerScreen() {
  return (
    <div style={{ padding: "12px 10px 60px" }}>
      <div style={{ marginBottom: 10 }}>
        <div style={{ fontSize: 12, fontWeight: 800, color: "#F2F2F7", marginBottom: 2 }}>
          Chip Scanner
        </div>
        <div style={{ fontSize: 8, color: "#8E8E93" }}>Point camera at your stack</div>
      </div>

      {/* Camera view simulation */}
      <div
        style={{
          borderRadius: 14,
          background: "#141418",
          border: "1px solid rgba(124,108,246,0.4)",
          padding: 12,
          marginBottom: 8,
          position: "relative",
          height: 140,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        {/* Corner markers */}
        {[
          { top: 6, left: 6 },
          { top: 6, right: 6 },
          { bottom: 6, left: 6 },
          { bottom: 6, right: 6 },
        ].map((pos, i) => (
          <div
            key={i}
            style={{
              position: "absolute",
              width: 12,
              height: 12,
              borderTop: i < 2 ? "2px solid #7C6CF6" : "none",
              borderBottom: i >= 2 ? "2px solid #7C6CF6" : "none",
              borderLeft: i % 2 === 0 ? "2px solid #7C6CF6" : "none",
              borderRight: i % 2 !== 0 ? "2px solid #7C6CF6" : "none",
              ...pos,
            }}
          />
        ))}

        {/* Chip stacks in camera view */}
        <div style={{ display: "flex", gap: 6, justifyContent: "center" }}>
          {[
            { color: "#FF9F0A", count: 6 },
            { color: "#7C6CF6", count: 8 },
            { color: "#34C759", count: 5 },
            { color: "#FF453A", count: 10 },
            { color: "#5AC8FA", count: 4 },
          ].map((stack, i) => (
            <div key={i} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 1 }}>
              {[...Array(stack.count)].map((_, j) => (
                <div
                  key={j}
                  style={{
                    width: 20,
                    height: 7,
                    borderRadius: "50%",
                    background: stack.color,
                    border: "1px solid rgba(255,255,255,0.15)",
                  }}
                />
              ))}
            </div>
          ))}
        </div>

        {/* Scan line */}
        <div
          style={{
            position: "absolute",
            left: 12,
            right: 12,
            height: 1,
            top: "45%",
            background: "linear-gradient(90deg, transparent, #7C6CF6, transparent)",
            opacity: 0.7,
          }}
        />
      </div>

      {/* Result */}
      <div
        style={{
          borderRadius: 12,
          background: "rgba(124,108,246,0.1)",
          border: "1px solid rgba(124,108,246,0.3)",
          padding: "10px 12px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div>
          <div style={{ fontSize: 18, fontWeight: 900, color: "#7C6CF6" }}>$3,250</div>
          <div style={{ fontSize: 8, color: "#8E8E93" }}>AI Detected • 98% confidence</div>
        </div>
        <div style={{ textAlign: "right" }}>
          <div style={{ fontSize: 8, color: "#AEAEB2", marginBottom: 2 }}>
            <span style={{ color: "#FF9F0A" }}>3×</span> $500
          </div>
          <div style={{ fontSize: 8, color: "#AEAEB2", marginBottom: 2 }}>
            <span style={{ color: "#7C6CF6" }}>8×</span> $100
          </div>
          <div style={{ fontSize: 8, color: "#AEAEB2" }}>
            <span style={{ color: "#34C759" }}>5×</span> $25
          </div>
        </div>
      </div>
    </div>
  );
}

function FinderScreen() {
  const cardRooms = [
    { name: "Bellagio Poker Room", dist: "0.2 mi", tables: 16, games: "$1/$3 · $2/$5 · $5/$10" },
    { name: "Aria Poker", dist: "0.4 mi", tables: 24, games: "$1/$3 · $2/$5 · PLO" },
    { name: "Wynn Poker", dist: "0.6 mi", tables: 18, games: "$1/$3 · $2/$5 · $5/$10" },
  ];

  return (
    <div style={{ padding: "12px 10px 60px" }}>
      <div style={{ marginBottom: 10 }}>
        <div style={{ fontSize: 12, fontWeight: 800, color: "#F2F2F7", marginBottom: 2 }}>
          Find Card Rooms
        </div>
        <div style={{ fontSize: 8, color: "#8E8E93" }}>Las Vegas, NV · 12 venues nearby</div>
      </div>

      {/* Search bar */}
      <div
        style={{
          borderRadius: 10,
          background: "#1C1C21",
          border: "1px solid #3A3A42",
          padding: "7px 10px",
          marginBottom: 8,
          display: "flex",
          alignItems: "center",
          gap: 5,
        }}
      >
        <div style={{ width: 8, height: 8, borderRadius: "50%", border: "1.5px solid #8E8E93" }} />
        <span style={{ fontSize: 8, color: "#636366" }}>Search card rooms...</span>
      </div>

      {/* List */}
      <div style={{ display: "flex", flexDirection: "column", gap: 5 }}>
        {cardRooms.map((room, i) => (
          <div
            key={i}
            style={{
              borderRadius: 12,
              background: "#1C1C21",
              border: "1px solid #2C2C34",
              padding: "9px 10px",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 9, fontWeight: 700, color: "#F2F2F7", marginBottom: 2 }}>
                {room.name}
              </div>
              <div style={{ fontSize: 7, color: "#5AC8FA", marginBottom: 2 }}>
                {room.games}
              </div>
              <div style={{ fontSize: 7, color: "#8E8E93" }}>
                {room.tables} tables open
              </div>
            </div>
            <div style={{ textAlign: "right", flexShrink: 0 }}>
              <div style={{ fontSize: 8, color: "#AEAEB2", marginBottom: 3 }}>{room.dist}</div>
              <div
                style={{
                  fontSize: 7,
                  color: "#7C6CF6",
                  background: "rgba(124,108,246,0.1)",
                  borderRadius: 4,
                  padding: "2px 5px",
                }}
              >
                Directions
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

const screenComponents: Record<string, React.FC> = {
  gallery: GalleryScreen,
  scanner: ScannerScreen,
  finder: FinderScreen,
};

interface ShowcasePhoneProps {
  screen: string;
  accent: string;
  isCenter?: boolean;
}

function ShowcasePhone({ screen, accent, isCenter = false }: ShowcasePhoneProps) {
  const ScreenComponent = screenComponents[screen];

  return (
    <div
      className="relative mx-auto"
      style={{
        width: isCenter ? 230 : 200,
        transform: isCenter ? "none" : `scale(0.9) translateY(${isCenter ? 0 : 20}px)`,
        transformOrigin: "top center",
      }}
    >
      {isCenter && (
        <div
          className="absolute pulse-glow"
          style={{
            inset: "-20px",
            background: `radial-gradient(ellipse at center, ${accent}25 0%, transparent 65%)`,
            filter: "blur(20px)",
          }}
        />
      )}
      <div
        style={{
          width: "100%",
          paddingBottom: "210%",
          borderRadius: 38,
          border: `2px solid ${isCenter ? accent + "60" : "#3A3A42"}`,
          background: "#0A0A0C",
          overflow: "hidden",
          position: "relative",
        }}
      >
        <div style={{ position: "absolute", inset: 0 }}>
              {/* Status bar */}
          <div style={{ display: "flex", justifyContent: "space-between", padding: "10px 16px 4px", alignItems: "center" }}>
            <span style={{ fontSize: 9, fontWeight: 600, color: "#F2F2F7" }}>9:41</span>
            <div style={{ width: 60, height: 18, background: "#000", borderRadius: 12 }} />
            <div style={{ width: 20, height: 8, borderRadius: 3, border: "1px solid rgba(255,255,255,0.4)" }}>
              <div style={{ width: "70%", height: "100%", background: "#34C759", borderRadius: 2 }} />
            </div>
          </div>
          <ScreenComponent />
        </div>
        {/* Bottom tab bar */}
        <div
          style={{
            position: "absolute",
            bottom: 0,
            left: 0,
            right: 0,
            background: "rgba(14,14,18,0.96)",
            borderTop: "1px solid #2C2C34",
            display: "flex",
            padding: "6px 0 16px",
          }}
        >
          {["Photos", "Sessions", "Find", "About"].map((tab, i) => (
            <div key={tab} style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 2 }}>
              <div
                style={{
                  width: 14,
                  height: 14,
                  borderRadius: 4,
                  background:
                    (screen === "gallery" && i === 0) ||
                    (screen === "scanner" && i === 1) ||
                    (screen === "finder" && i === 2)
                      ? accent
                      : "#3A3A42",
                  opacity:
                    (screen === "gallery" && i === 0) ||
                    (screen === "scanner" && i === 1) ||
                    (screen === "finder" && i === 2)
                      ? 1
                      : 0.4,
                }}
              />
              <span
                style={{
                  fontSize: 6,
                  color:
                    (screen === "gallery" && i === 0) ||
                    (screen === "scanner" && i === 1) ||
                    (screen === "finder" && i === 2)
                      ? accent
                      : "#636366",
                  fontWeight:
                    (screen === "gallery" && i === 0) ||
                    (screen === "scanner" && i === 1) ||
                    (screen === "finder" && i === 2)
                      ? 600
                      : 400,
                }}
              >
                {tab}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default function AppShowcase() {
  return (
    <section className="py-24 relative overflow-hidden" style={{ background: "#0A0A0C" }}>
      {/* Background */}
      <div
        className="absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse 80% 50% at 50% 50%, rgba(124,108,246,0.05) 0%, transparent 60%)",
        }}
      />

      <div className="relative max-w-6xl mx-auto px-4 sm:px-6">
        <div className="text-center mb-16">
          <p
            className="text-sm font-semibold uppercase tracking-widest mb-3"
            style={{ color: "#7C6CF6" }}
          >
            Inside the App
          </p>
          <h2 className="text-3xl sm:text-4xl font-black text-stax-text mb-4">
            Every screen is built for the felt
          </h2>
          <p className="text-stax-text-muted max-w-lg mx-auto text-base leading-relaxed">
            Clean, dark, distraction-free. STAX is designed to look as good as
            your stack.
          </p>
        </div>

        {/* Three phone showcase - desktop shows all 3, mobile shows center */}
        <div className="flex items-start justify-center gap-4 md:gap-8 mb-16 overflow-hidden">
          {/* Left phone - hidden on small screens */}
          <div className="hidden md:block flex-shrink-0">
            <ShowcasePhone
              screen={showcaseItems[0].screen}
              accent={showcaseItems[0].accent}
              isCenter={false}
            />
          </div>
          {/* Center phone - always shown */}
          <div className="flex-shrink-0">
            <ShowcasePhone
              screen={showcaseItems[1].screen}
              accent={showcaseItems[1].accent}
              isCenter={true}
            />
          </div>
          {/* Right phone - hidden on small screens */}
          <div className="hidden md:block flex-shrink-0">
            <ShowcasePhone
              screen={showcaseItems[2].screen}
              accent={showcaseItems[2].accent}
              isCenter={false}
            />
          </div>
        </div>

        {/* Feature labels below phones */}
        <div className="grid grid-cols-3 gap-6 max-w-2xl mx-auto">
          {showcaseItems.map((item) => (
            <div key={item.title} className="text-center">
              <h3
                className="text-sm font-bold mb-1"
                style={{ color: item.accent }}
              >
                {item.title}
              </h3>
              <p className="text-xs text-stax-text-muted leading-relaxed hidden md:block">
                {item.description}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
