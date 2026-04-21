import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "STAX — Poker Session & Chip Tracking App",
  description:
    "The ultimate poker companion. Capture chip stacks, track sessions, scan chips with AI, and find card rooms near you. Poker Porn, No Shame.",
  keywords: [
    "poker app",
    "chip tracker",
    "poker session tracker",
    "chip stack",
    "poker photo gallery",
    "casino finder",
    "poker chips",
    "stax app",
  ],
  openGraph: {
    title: "STAX — Poker Session & Chip Tracking App",
    description:
      "Capture chip stacks, track sessions, scan chips with AI, and find card rooms near you.",
    type: "website",
    siteName: "STAX",
  },
  twitter: {
    card: "summary_large_image",
    title: "STAX — Poker Session & Chip Tracking App",
    description:
      "The ultimate poker companion. Poker Porn, No Shame.",
  },
  robots: "index, follow",
  icons: {
    icon: "/favicon.ico",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body
        className={`${geistSans.variable} ${geistMono.variable} font-sans antialiased bg-stax-bg text-stax-text`}
      >
        {children}
      </body>
    </html>
  );
}
