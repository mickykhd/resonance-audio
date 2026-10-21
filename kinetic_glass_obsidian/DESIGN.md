---
name: Kinetic Glass Obsidian
colors:
  surface: '#111318'
  surface-dim: '#111318'
  surface-bright: '#37393f'
  surface-container-lowest: '#0c0e13'
  surface-container-low: '#1a1b21'
  surface-container: '#1e1f25'
  surface-container-high: '#282a2f'
  surface-container-highest: '#33353a'
  on-surface: '#e2e2e9'
  on-surface-variant: '#cbc3d7'
  inverse-surface: '#e2e2e9'
  inverse-on-surface: '#2e3036'
  outline: '#958ea0'
  outline-variant: '#494454'
  surface-tint: '#d0bcff'
  primary: '#d0bcff'
  on-primary: '#3c0091'
  primary-container: '#a078ff'
  on-primary-container: '#340080'
  inverse-primary: '#6d3bd7'
  secondary: '#4cd7f6'
  on-secondary: '#003640'
  secondary-container: '#03b5d3'
  on-secondary-container: '#00424e'
  tertiary: '#ffb2b7'
  on-tertiary: '#67001b'
  tertiary-container: '#ff516a'
  on-tertiary-container: '#5b0017'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e9ddff'
  primary-fixed-dim: '#d0bcff'
  on-primary-fixed: '#23005c'
  on-primary-fixed-variant: '#5516be'
  secondary-fixed: '#acedff'
  secondary-fixed-dim: '#4cd7f6'
  on-secondary-fixed: '#001f26'
  on-secondary-fixed-variant: '#004e5c'
  tertiary-fixed: '#ffdadb'
  tertiary-fixed-dim: '#ffb2b7'
  on-tertiary-fixed: '#40000d'
  on-tertiary-fixed-variant: '#92002a'
  background: '#111318'
  on-background: '#e2e2e9'
  surface-variant: '#33353a'
typography:
  display-lg:
    fontFamily: Space Grotesk
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
  display-lg-mobile:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Space Grotesk
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
  label-md:
    fontFamily: Space Grotesk
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Space Grotesk
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 12px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes a high-performance, immersive audio-visual experience tailored for Android enthusiasts and audiophiles. Rooted in an evolved dark-mode paradigm, it merges modern Material You dynamic principles with deep obsidian glassmorphism.

### Brand Personality & Core Mood
- **Hyper-Tactile & Fluid:** Every action responds with low-latency visual precision, mirroring real-time sound waves and liquid playback responses.
- **Audiophile Fidelity:** Technical competence conveyed through crisp telemetry, precise audio waveform scrubbers, and instantaneous metadata display.
- **Atmospheric & Night-Optimized:** Zero light pollution; OLED-optimized deep charcoal grounds contrasting with razor-sharp emissive accents.

### Design Language & Movement
The design movement sits at the confluence of **Refined Glassmorphism** and **Material Expressive**. Translucent multi-stop surfaces hover over deep tonal obsidian wells. Contrast is maintained through inner specular borders (1px hairline strokes) and electric violet and cyan illumination rather than diffuse mud-colored drop shadows.

## Colors

The palette is engineered specifically for deep-contrast OLED environments, balancing power efficiency with luminous acoustic accents.

### Color Tiers & Hierarchy
- **Primary (`#8B5CF6` - Electric Violet):** Represents core playback energy, active track states, primary floating action controls, and major focal selections.
- **Secondary (`#06B6D4` - Neon Cyan):** Drives audio waveforms, live equalizers, buffer bars, active stream toggles, and metadata highlights.
- **Tertiary (`#F43F5E` - Vivid Rose):** Reserved strictly for favorite states, explicit content badges, and destructive alerts.
- **Neutral Canvas (`#0D0F14` - Obsidian Base):** The non-reflective structural root. Stacked with surface tiers:
  - Surface Tier 1 (Background): `#0D0F14`
  - Surface Tier 2 (Panels & Bottom Sheet): `#151922`
  - Surface Tier 3 (Cards & Floating Modals): `#1E2330`
  - Glass Containers: `rgba(30, 35, 48, 0.65)` with `backdrop-filter: blur(24px)`

### System Feedback & Signal Colors
- **Storage Scanner Status (Indexing/Success):** `#10B981` (Emerald).
- **Online Streaming Toggle (Active/Synced):** `#06B6D4` (Neon Cyan).
- **Offline / Local Audio Only:** `#64748B` (Muted Slate).

## Typography

Typography balances technical, metric-driven punch with effortless legibility across track titles and dense playlist layouts.

- **Headline & Display (`Space Grotesk`):** Delivers a crisp, modern audio-hardware presence. Its tight geometric cuts excel in track titles, duration timestamps, sample rates (e.g., `FLAC • 24-bit / 96kHz`), and dynamic album headings.
- **Body (`Plus Jakarta Sans`):** Warm, humanist geometry that maintains exceptional comfort for long-reading liner notes, artist bios, and nested queue menus.
- **Labels & Micro-indicators (`Space Grotesk`):** Provides monospaced-adjacent legibility for tracking times, bitrates, audio file tags, and quick-pill filters.

## Layout & Spacing

The structural layout is tuned for modern Android hand ergonomics, specifically optimized around a 390px reference viewport width with adaptive fluid scaling across foldable devices and tablets.

### Fluid 4-to-8 Column Rhythm
- **Mobile (320px – 599px):** 4-column fluid layout with an edge margin of `16px` (`margin-mobile`), column gutters of `12px` (`gutter-mobile`), and dominant thumb-reach zones anchored to the bottom 45% of the screen.
- **Tablet / Large Unfolded (600px+):** 8-column layout with `24px` margins, shifting the Now Playing player to a persistent split-pane alongside the library view.

### Density & Vertical Rythm
- Components stack with `space-sm` (8px) for tightly coupled items (track titles to artist names) and `space-md` (16px) for item rows.
- Dynamic padding accommodates Android system bars: an automated bottom offset equal to `navigation_bar_height + space-xl` prevents the persistent Mini-Player from colliding with Android gesture navigation lines.

## Elevation & Depth

Visual hierarchy uses translucent optical layering and inner specular reflections rather than muddy multi-layered drop shadows.

### Atmospheric Glass Layering
- **Level 0 (Base Canvas):** Solid `#0D0F14`. Non-reactive backdrop.
- **Level 1 (Docked Surfaces & Drawers):** `#151922` with 80% opacity and an active `backdrop-filter: blur(20px)`. Includes a top hairline stroke: `1px solid rgba(255, 255, 255, 0.06)`.
- **Level 2 (Cards & Floating Mini-Player):** `rgba(30, 35, 48, 0.72)` supported by a dual-border technique:
  - Outside: `1px solid rgba(139, 92, 246, 0.15)`
  - Inside inset: `1px solid rgba(255, 255, 255, 0.08)`
  - Shadow: `0 12px 32px -4px rgba(0, 0, 0, 0.5)`
- **Level 3 (Modals, Context Sheets, Player Overlay):** `rgba(21, 25, 34, 0.94)` with `backdrop-filter: blur(36px)`.

### Emissive Accents
Active controls cast localized color fields. The primary play button emits a concentrated glow:
- Box shadow: `0 0 24px rgba(139, 92, 246, 0.45), 0 4px 12px rgba(0, 0, 0, 0.35)`.

## Shapes

The design system embraces an intentional dual-curvature hierarchy, contrasting large structural containers with fully pill-shaped controls.

### Curvature Assignments
- **Track Artwork & Glass Cards:** `rounded-2xl` (16px / 1rem) to soften large media visuals while retaining crisp grid boundaries.
- **Action Controls & Floating Elements:** `rounded-full` (9999px) for Play/Pause floating actions, scrubber heads, interactive chips, and floating mini-players.
- **Bottom Sheets & Modal Surfaces:** Asymmetric rounding with `24px` on top-left and top-right radii, transitioning cleanly into screen edges.

## Components

### Buttons & Interactive Controls
- **Primary Playback FAB:** 64x64px circular button (`rounded-full`) clad in Electric Violet (`#8B5CF6`). Contains pure white icons with a directional gradient highlight (`rgba(255, 255, 255, 0.2)` to transparent). Emits an ambient purple aura when playing.
- **Secondary Actions (Skip, Shuffle, Repeat):** 40x40px circular touch targets, transparent background, `#94A3B8` icon default, transitioning to Neon Cyan (`#06B6D4`) with a glowing dot indicator when toggled active.

### Audio Waveform Scrubber
- Consists of 48-64 vertical sound-level bars rendered with `rounded-full`.
- **Played State:** Neon Cyan (`#06B6D4`) with variable height opacity.
- **Unplayed State:** `rgba(255, 255, 255, 0.12)`.
- **Scrub Thumb:** 14px circular indicator ring glowing with violet and cyan specular reflections.

### Track Lists & Row Items
- **Container:** Height 64px, fluid width. Background remains transparent, morphing to `rgba(255, 255, 255, 0.04)` on active press.
- **Artwork Thumbnail:** 48x48px with `rounded-xl` and a subtle 1px inner border.
- **Active Track Highlight:** The active playing item displays an animated 3-bar cyan equalizer glyph adjacent to the title, with title text tinted `#8B5CF6`.

### Glassmorphic Mini-Player
- Floats 8px above the bottom navigation bar.
- Height: 60px, roundedness: `rounded-2xl`.
- Surface: Glass obsidian (`rgba(30, 35, 48, 0.75)` with 20px blur).
- Integrated progress bar: 2px hairline line anchored seamlessly to the bottom edge of the mini-player card.

### System Indicators & Toggles
- **Local Storage Scanner Chip:** Pill container (`rounded-full`) styled with `rgba(16, 185, 129, 0.1)` background, `#10B981` text, and a pulsing 6px emerald dot indicating active metadata library scans.
- **Streaming Mode Toggle:** Segmented pill toggle: "Local" vs "Online". Active selection receives obsidian surface fill (`#1E2330`) with an inset cyan border and bright white text.

### Filter Chips
- `rounded-full` height 32px with horizontal padding of 14px.
- Inactive: Background `#151922`, border `1px solid rgba(255, 255, 255, 0.08)`, text `#94A3B8`.
- Active: Background `rgba(139, 92, 246, 0.15)`, border `1px solid #8B5CF6`, text `#FFFFFF`.