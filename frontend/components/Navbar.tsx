"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Menu, X } from "lucide-react";
import StaxLogo from "./StaxLogo";

const navLinks = [
  { label: "Features", href: "#features" },
  { label: "Screenshots", href: "/screenshots" },
  { label: "Pricing", href: "#pricing" },
  { label: "Download", href: "#download" },
];

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 24);
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <header
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled
          ? "border-b"
          : "bg-transparent border-b border-transparent"
      }`}
      style={
        scrolled
          ? {
              background: "rgba(10,10,12,0.88)",
              backdropFilter: "blur(20px) saturate(180%)",
              WebkitBackdropFilter: "blur(20px) saturate(180%)",
              borderColor: "rgba(58,58,66,0.5)",
            }
          : undefined
      }
    >
      <nav className="max-w-6xl mx-auto px-4 sm:px-6 h-[68px] flex items-center justify-between gap-8">
        {/* Brand */}
        <Link href="/" className="flex items-center gap-2.5 group shrink-0">
          <StaxLogo size={40} />
          <span
            className="text-[1.2rem] font-black tracking-tight transition-colors"
            style={{ color: "#F2F2F7", letterSpacing: "-0.01em" }}
          >
            STAX
          </span>
        </Link>

        {/* Desktop nav links */}
        <ul className="hidden md:flex items-center gap-7">
          {navLinks.map((link) => (
            <li key={link.label}>
              <Link href={link.href} className="nav-link">
                {link.label}
              </Link>
            </li>
          ))}
        </ul>

        {/* Desktop CTA */}
        <div className="hidden md:block shrink-0">
          <Link href="#download" className="btn btn-primary" style={{ padding: "9px 20px", borderRadius: "10px", fontSize: "0.8125rem" }}>
            Get the App
          </Link>
        </div>

        {/* Mobile toggle */}
        <button
          className="md:hidden p-1 transition-colors"
          style={{ color: "#AEAEB2" }}
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle menu"
        >
          {mobileOpen ? <X size={21} /> : <Menu size={21} />}
        </button>
      </nav>

      {/* Mobile drawer */}
      <div
        className={`md:hidden overflow-hidden transition-all duration-300 ${
          mobileOpen ? "max-h-screen opacity-100" : "max-h-0 opacity-0"
        }`}
        style={{
          background: "rgba(10,10,12,0.97)",
          backdropFilter: "blur(20px)",
          WebkitBackdropFilter: "blur(20px)",
          borderBottom: mobileOpen ? "1px solid rgba(58,58,66,0.4)" : "none",
        }}
      >
        <ul className="flex flex-col px-4 pt-2 pb-5 gap-0.5">
          {navLinks.map((link) => (
            <li key={link.label}>
              <Link
                href={link.href}
                onClick={() => setMobileOpen(false)}
                className="flex items-center py-3.5 text-sm font-medium border-b transition-colors"
                style={{
                  color: "#AEAEB2",
                  borderColor: "rgba(58,58,66,0.3)",
                }}
              >
                {link.label}
              </Link>
            </li>
          ))}
          <li className="pt-4">
            <Link
              href="#download"
              onClick={() => setMobileOpen(false)}
              className="btn btn-primary w-full"
            >
              Get the App
            </Link>
          </li>
        </ul>
      </div>
    </header>
  );
}
