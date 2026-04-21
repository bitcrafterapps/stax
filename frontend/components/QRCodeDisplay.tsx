"use client";

import { QRCodeSVG } from "qrcode.react";

interface QRCodeDisplayProps {
  size?: number;
  url?: string;
}

export default function QRCodeDisplay({
  size = 120,
  url = "https://staxapp.io/download",
}: QRCodeDisplayProps) {
  return (
    <div
      className="rounded-xl p-2 inline-block"
      style={{ background: "white" }}
    >
      <QRCodeSVG
        value={url}
        size={size}
        bgColor="#FFFFFF"
        fgColor="#0A0A0C"
        level="M"
      />
    </div>
  );
}
