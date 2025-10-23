import type { Metadata } from "next";
import "./globals.css";

import NavBar from "@/components/NavBar";
import Footer from "@/components/Footer";

export const metadata: Metadata = {
  title: "TrackMatch",
  description: "A Music Recognition System.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body
        className="flex flex-col min-h-screen antialiased dark"
      >
        <NavBar />
        <div className="flex-grow p-14 mb-14">
          {children}
        </div>
        <Footer />
      </body>
    </html>
  );
}
