import Image from "next/image";

interface PhoneMockupProps {
  size?: number;
}

export default function PhoneMockup({ size = 292 }: PhoneMockupProps) {
  // Scale all phone dimensions proportionally to base 292px
  const scale = size / 292;
  const outerRadius = 34 * scale;
  const innerRadius = 32 * scale;
  const notchW = 92 * scale;
  const notchH = 24 * scale;
  const notchTop = 10 * scale;
  const buttonW = Math.max(2, 3 * scale);

  return (
    <div className="relative mx-auto" style={{ width: size }}>

      {/* Outermost ambient halo */}
      <div
        className="absolute pointer-events-none pulse-glow"
        style={{
          inset: `-${60 * scale}px`,
          background:
            "radial-gradient(ellipse at 55% 45%, rgba(124,108,246,0.22) 0%, rgba(91,79,212,0.1) 40%, transparent 70%)",
          filter: "blur(8px)",
        }}
      />

      {/* Inner glow */}
      <div
        className="absolute pointer-events-none"
        style={{
          inset: `-${20 * scale}px`,
          background:
            "radial-gradient(ellipse at 50% 40%, rgba(124,108,246,0.14) 0%, transparent 65%)",
          filter: "blur(4px)",
        }}
      />

      {/* Phone chassis */}
      <div
        className="relative"
        style={{
          width: size,
          borderRadius: outerRadius,
          padding: "2px",
          background: "linear-gradient(160deg, #4A4A56 0%, #2A2A32 40%, #1A1A20 100%)",
          boxShadow:
            "0 40px 80px rgba(0,0,0,0.7), 0 0 0 1px rgba(255,255,255,0.06), inset 0 1px 0 rgba(255,255,255,0.08)",
        }}
      >
        <div
          className="phone-shine relative overflow-hidden"
          style={{
            borderRadius: innerRadius,
            background: "#050507",
          }}
        >
          {/* Notch / Dynamic Island */}
          <div
            style={{
              position: "absolute",
              top: notchTop,
              left: "50%",
              transform: "translateX(-50%)",
              width: notchW,
              height: notchH,
              background: "#000",
              borderRadius: 999,
              zIndex: 10,
              boxShadow: "0 0 0 1px rgba(255,255,255,0.04)",
            }}
          />

          <Image
            src="/screenshots/857.jpg"
            alt="STAX app — Casino sessions dashboard"
            width={473}
            height={1006}
            priority
            style={{ width: "100%", height: "auto", display: "block" }}
          />
        </div>
      </div>

      {/* Volume buttons */}
      <div
        style={{
          position: "absolute",
          left: -buttonW,
          top: "24%",
          width: buttonW,
          height: 32 * scale,
          borderRadius: `${buttonW}px 0 0 ${buttonW}px`,
          background: "linear-gradient(180deg, #3A3A42, #2A2A30)",
        }}
      />
      <div
        style={{
          position: "absolute",
          left: -buttonW,
          top: "34%",
          width: buttonW,
          height: 28 * scale,
          borderRadius: `${buttonW}px 0 0 ${buttonW}px`,
          background: "linear-gradient(180deg, #3A3A42, #2A2A30)",
        }}
      />
      {/* Power button */}
      <div
        style={{
          position: "absolute",
          right: -buttonW,
          top: "28%",
          width: buttonW,
          height: 44 * scale,
          borderRadius: `0 ${buttonW}px ${buttonW}px 0`,
          background: "linear-gradient(180deg, #3A3A42, #2A2A30)",
        }}
      />
    </div>
  );
}
