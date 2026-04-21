import type { Metadata } from "next";
import Link from "next/link";
import StaxLogo from "@/components/StaxLogo";

export const metadata: Metadata = {
  title: "Terms of Service — STAX",
  description: "Terms of Service for the STAX poker session and chip tracking app.",
};

export default function TermsPage() {
  return (
    <div
      className="min-h-screen"
      style={{ background: "#0A0A0C", color: "#F2F2F7" }}
    >
      {/* Header */}
      <header className="border-b" style={{ borderColor: "#1C1C21" }}>
        <div className="max-w-3xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2">
            <StaxLogo size={28} />
            <span className="text-lg font-black" style={{ color: "#F2F2F7" }}>
              STAX
            </span>
          </Link>
          <Link
            href="/"
            className="text-sm font-medium transition-colors"
            style={{ color: "#AEAEB2" }}
          >
            ← Back to Home
          </Link>
        </div>
      </header>

      {/* Content */}
      <main className="max-w-3xl mx-auto px-4 sm:px-6 py-16">
        <div className="mb-10">
          <h1 className="text-3xl sm:text-4xl font-black mb-3">
            Terms of Service
          </h1>
          <p style={{ color: "#AEAEB2" }} className="text-sm">
            Last updated: April 20, 2026
          </p>
        </div>

        <div className="prose-custom space-y-8">
          <Section title="Agreement to Terms">
            <p>
              By downloading, installing, or using the STAX app (&quot;App&quot;),
              you agree to be bound by these Terms of Service. If you do not
              agree to these terms, do not use the App.
            </p>
            <p>
              These Terms apply to all users of the App. STAX is operated by
              Bitcraft Apps.
            </p>
          </Section>

          <Section title="Description of Service">
            <p>
              STAX is a mobile application that allows users to:
            </p>
            <ul>
              <li>Create and manage poker session records</li>
              <li>Capture and organize photos of poker chip stacks</li>
              <li>Scan and count poker chips using on-device AI</li>
              <li>
                Use optional cloud-based chip counting via third-party AI
                services
              </li>
              <li>Find card rooms and casinos near their location</li>
            </ul>
          </Section>

          <Section title="Free and Premium Tiers">
            <p>
              STAX offers a free tier and a premium subscription:
            </p>
            <ul>
              <li>
                <strong style={{ color: "#F2F2F7" }}>Free:</strong> Core
                features with usage limits, including 3 active sessions, 10
                photos per session, and 5 chip scans per day.
              </li>
              <li>
                <strong style={{ color: "#F2F2F7" }}>Premium:</strong> $4.99/month
                or $39/year, with a 7-day free trial. Includes unlimited
                sessions, photos, scans, and AI-powered chip counting.
              </li>
            </ul>
            <p>
              Subscriptions are billed through the Apple App Store or Google
              Play Store and are subject to those platforms&apos; billing terms.
              You may cancel at any time.
            </p>
          </Section>

          <Section title="User Conduct">
            <p>You agree not to:</p>
            <ul>
              <li>Use the App for any unlawful purpose</li>
              <li>
                Attempt to reverse engineer, decompile, or disassemble the App
              </li>
              <li>
                Use the App in a way that could damage, disable, or impair its
                functionality
              </li>
              <li>
                Submit false or misleading information through the App
              </li>
            </ul>
          </Section>

          <Section title="Intellectual Property">
            <p>
              The App, its design, features, and content are owned by Bitcraft
              Apps and protected by applicable intellectual property laws. You
              are granted a limited, non-exclusive license to use the App for
              personal, non-commercial purposes.
            </p>
            <p>
              Content you create within the App (photos, session data) remains
              yours. You grant Bitcraft Apps no rights to your content beyond
              what is needed to provide the App service on your device.
            </p>
          </Section>

          <Section title="Third-Party Services">
            <p>
              STAX integrates with optional third-party services:
            </p>
            <ul>
              <li>
                <strong style={{ color: "#F2F2F7" }}>OpenAI:</strong> Used for
                optional cloud chip estimation. When enabled, images are sent
                to OpenAI and processed under their terms.
              </li>
              <li>
                <strong style={{ color: "#F2F2F7" }}>Google Maps:</strong> Used
                for directions to card rooms. Subject to Google&apos;s terms when
                accessed.
              </li>
            </ul>
            <p>
              We are not responsible for the actions or content of third-party
              services.
            </p>
          </Section>

          <Section title="Disclaimers">
            <p>
              The App is provided &quot;as is&quot; without warranties of any kind.
              Bitcraft Apps does not warrant that the App will be error-free,
              uninterrupted, or meet your specific requirements.
            </p>
            <p>
              STAX is intended for entertainment and personal tracking purposes.
              Chip counts provided by the scanner are estimates and should not
              be relied upon for financial accuracy at the poker table.
            </p>
            <p>
              STAX does not endorse, facilitate, or assist with illegal gambling
              activity. Users are responsible for complying with all local laws
              regarding gambling.
            </p>
          </Section>

          <Section title="Limitation of Liability">
            <p>
              To the maximum extent permitted by law, Bitcraft Apps shall not
              be liable for any indirect, incidental, special, consequential, or
              punitive damages arising from your use of the App, even if we have
              been advised of the possibility of such damages.
            </p>
          </Section>

          <Section title="Changes to These Terms">
            <p>
              We may update these Terms from time to time. We will notify users
              of material changes through an in-app notice or by updating the
              &quot;Last updated&quot; date above. Continued use of the App after changes
              constitutes acceptance of the new Terms.
            </p>
          </Section>

          <Section title="Governing Law">
            <p>
              These Terms shall be governed by and construed in accordance with
              the laws of the United States, without regard to its conflict of
              law provisions.
            </p>
          </Section>

          <Section title="Contact">
            <p>For questions about these Terms, contact us at:</p>
            <ul>
              <li>Developer: Bitcraft Apps</li>
              <li>
                Email:{" "}
                <a
                  href="mailto:legal@staxapp.io"
                  style={{ color: "#7C6CF6" }}
                >
                  legal@staxapp.io
                </a>
              </li>
            </ul>
          </Section>
        </div>

        <div className="mt-16 pt-8" style={{ borderTop: "1px solid #1C1C21" }}>
          <Link
            href="/"
            className="text-sm font-medium transition-colors"
            style={{ color: "#7C6CF6" }}
          >
            ← Back to STAX
          </Link>
        </div>
      </main>

      {/* Inline prose styles */}
      <style>{`
        .prose-custom p {
          color: #AEAEB2;
          line-height: 1.75;
          margin-bottom: 1rem;
          font-size: 0.9375rem;
        }
        .prose-custom ul {
          list-style: disc;
          padding-left: 1.5rem;
          margin-bottom: 1rem;
        }
        .prose-custom li {
          color: #AEAEB2;
          line-height: 1.7;
          margin-bottom: 0.35rem;
          font-size: 0.9375rem;
        }
        .prose-custom a:hover {
          opacity: 0.8;
        }
      `}</style>
    </div>
  );
}

function Section({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <h2 className="text-lg font-bold mb-4" style={{ color: "#F2F2F7" }}>
        {title}
      </h2>
      <div className="prose-custom">{children}</div>
    </div>
  );
}
