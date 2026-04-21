import type { Metadata } from "next";
import Link from "next/link";
import StaxLogo from "@/components/StaxLogo";

export const metadata: Metadata = {
  title: "Privacy Policy — STAX",
  description: "Privacy Policy for the STAX poker session and chip tracking app.",
};

export default function PrivacyPage() {
  return (
    <div
      className="min-h-screen"
      style={{ background: "#0A0A0C", color: "#F2F2F7" }}
    >
      {/* Header */}
      <header
        className="border-b"
        style={{ borderColor: "#1C1C21" }}
      >
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
            Privacy Policy
          </h1>
          <p style={{ color: "#AEAEB2" }} className="text-sm">
            Last updated: April 20, 2026
          </p>
        </div>

        <div className="prose-custom space-y-8">
          <Section title="Summary">
            <p>
              STAX is a poker session and chip-tracking app. Most app data is
              stored on your device. Some optional features use third-party
              services only when you turn them on. We do not sell your personal
              information.
            </p>
          </Section>

          <Section title="Data We Store On Your Device">
            <p>STAX stores the following data locally on your device:</p>
            <ul>
              <li>
                Session details you create, such as casino name, state, game
                type, buy-ins, cash-outs, and notes
              </li>
              <li>Photos you capture or attach to sessions</li>
              <li>Chip configuration settings</li>
              <li>
                App preferences, including whether cloud chip estimation is
                enabled
              </li>
              <li>
                Your OpenAI API key, if you choose to save one
              </li>
            </ul>
            <p>
              Your OpenAI API key is stored only on your device and is not sent
              to a STAX-operated backend.
            </p>
          </Section>

          <Section title="Camera and Photos">
            <p>STAX requests camera access so you can:</p>
            <ul>
              <li>Capture session photos</li>
              <li>Scan poker chips from the camera view</li>
            </ul>
            <p>
              Photos you choose to keep are stored on your device as part of
              your session history.
            </p>
          </Section>

          <Section title="Location">
            <p>
              STAX may request approximate or precise location access so it can:
            </p>
            <ul>
              <li>Find nearby card rooms and casinos</li>
              <li>Suggest state information for session entry</li>
            </ul>
            <p>
              Location is used in-app for these features and is not stored as a
              persistent location history by STAX.
            </p>
          </Section>

          <Section title="Cloud Chip Estimation">
            <p>
              STAX includes an optional cloud chip estimation feature. When you
              enable this feature and start a cloud scan:
            </p>
            <ul>
              <li>
                The current scan image is sent directly from your device to
                OpenAI
              </li>
              <li>
                Your saved OpenAI API key is used to authorize that request
              </li>
              <li>The request is not routed through a STAX server</li>
            </ul>
            <p>
              OpenAI processes the image according to its own terms and privacy
              practices. Review{" "}
              <a
                href="https://openai.com/policies/privacy-policy"
                target="_blank"
                rel="noopener noreferrer"
                style={{ color: "#7C6CF6" }}
              >
                OpenAI&apos;s current policies
              </a>{" "}
              before enabling this feature.
            </p>
          </Section>

          <Section title="Sharing and Disclosure">
            <p>STAX does not sell your personal information.</p>
            <p>
              STAX shares data only when needed to provide an optional feature
              you choose to use, such as:
            </p>
            <ul>
              <li>OpenAI, for optional cloud chip estimation</li>
              <li>
                Google Maps or a browser, when you open directions links from
                the app
              </li>
            </ul>
          </Section>

          <Section title="Data Retention">
            <p>
              Data you create in STAX remains on your device until you delete it
              or uninstall the app, subject to normal device backup and storage
              behavior.
            </p>
            <p>
              OpenAI API keys saved by STAX are excluded from Android backup and
              device transfer rules in the Android app configuration.
            </p>
          </Section>

          <Section title="Security">
            <p>
              STAX keeps app data on-device where practical. No method of
              electronic storage or transmission is completely secure, so STAX
              cannot guarantee absolute security.
            </p>
          </Section>

          <Section title="Children's Privacy">
            <p>STAX is not directed to children under the age of 13.</p>
          </Section>

          <Section title="Your Choices">
            <p>You can:</p>
            <ul>
              <li>Decline camera or location permissions</li>
              <li>Leave cloud chip estimation disabled</li>
              <li>Remove your OpenAI API key from the app settings</li>
              <li>Delete sessions and photos from within the app</li>
              <li>
                Uninstall the app to remove locally stored app data
              </li>
            </ul>
          </Section>

          <Section title="Contact">
            <p>
              For privacy-related questions, contact us at:
            </p>
            <ul>
              <li>Developer: Bitcraft Apps</li>
              <li>
                Email:{" "}
                <a
                  href="mailto:privacy@staxapp.io"
                  style={{ color: "#7C6CF6" }}
                >
                  privacy@staxapp.io
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
          space-y: 0.5rem;
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
      <h2
        className="text-lg font-bold mb-4"
        style={{ color: "#F2F2F7" }}
      >
        {title}
      </h2>
      <div className="prose-custom">{children}</div>
    </div>
  );
}
