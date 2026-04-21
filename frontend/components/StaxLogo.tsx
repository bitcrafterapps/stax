import Image from "next/image";

interface StaxLogoProps {
  className?: string;
  size?: number;
}

export default function StaxLogo({ className = "", size = 40 }: StaxLogoProps) {
  return (
    <Image
      src="/stax-logo.png"
      alt="STAX Logo"
      width={size}
      height={size}
      className={className}
      priority
    />
  );
}
