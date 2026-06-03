import { LandingNav } from "@/components/landing/nav"
import { LandingHero } from "@/components/landing/hero"
import { LandingStats } from "@/components/landing/stats"
import { LandingPlatform } from "@/components/landing/platform"
import { LandingModules } from "@/components/landing/modules"
import { LandingRoles } from "@/components/landing/roles"
import { LandingSecurity } from "@/components/landing/security"
import { LandingCTA } from "@/components/landing/cta"
import { LandingFooter } from "@/components/landing/footer"

export default function HomePage() {
  return (
    <main className="bg-background">
      <LandingNav />
      <LandingHero />
      <LandingStats />
      <LandingPlatform />
      <LandingModules />
      <LandingRoles />
      <LandingSecurity />
      <LandingCTA />
      <LandingFooter />
    </main>
  )
}
