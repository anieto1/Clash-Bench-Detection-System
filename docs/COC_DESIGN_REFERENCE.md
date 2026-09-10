# Clash of Clans Design Reference Guide

> Design bible for the CBDS (Clash Bench Detection System) frontend.
> Goal: Capture the Clash of Clans visual language and adapt it for a modern CWL tracker web dashboard.

---

## Table of Contents

1. [Color Palette](#1-color-palette)
2. [Typography](#2-typography)
3. [Gradients](#3-gradients)
4. [Shadows & Glows](#4-shadows--glows)
5. [Borders & Outlines](#5-borders--outlines)
6. [Shapes & Layout](#6-shapes--layout)
7. [Buttons](#7-buttons)
8. [Cards & Containers](#8-cards--containers)
9. [Navigation](#9-navigation)
10. [Icons & Imagery](#10-icons--imagery)
11. [Textures & Backgrounds](#11-textures--backgrounds)
12. [In-Game UI Components](#12-in-game-ui-components)
13. [CWL-Specific UI Elements](#13-cwl-specific-ui-elements)
14. [Web Dashboard Adaptation Strategy](#14-web-dashboard-adaptation-strategy)
15. [CSS Component Library](#15-css-component-library)
16. [External Resources & Assets](#16-external-resources--assets)

---

## 1. Color Palette

### Primary Colors (Gold/Yellow Family)
The dominant CoC identity color. Used for headers, borders, accents, and interactive elements.

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| CoC Gold | `#F5C518` | 245, 197, 24 | Primary brand gold, title text, star icons |
| Rich Gold | `#DAA520` | 218, 165, 32 | Metallic border treatments, icon fills |
| Dark Gold | `#B8860B` | 184, 134, 11 | Border shadows, gold border dark edge |
| Light Gold | `#FFD700` | 255, 215, 0 | Highlights, hover states on gold elements |
| Pale Gold | `#FFEAA7` | 255, 234, 167 | Subtle gold tints, light backgrounds |
| Amber | `#F59E0B` | 245, 158, 11 | Warning states, resource indicators |

### Background Colors (Blue/Dark Family)
The characteristic deep blue backgrounds that dominate both the website and in-game UI.

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| Deep Navy | `#0A1628` | 10, 22, 40 | Darkest background, page base |
| CoC Blue | `#1A2744` | 26, 39, 68 | Primary dark background |
| Panel Blue | `#1E3A5F` | 30, 58, 95 | Card/panel backgrounds |
| Mid Blue | `#2B4A7A` | 43, 74, 122 | Secondary panels, hover states |
| Sky Blue | `#3B82F6` | 59, 130, 246 | Links, interactive highlights |
| Light Blue | `#60A5FA` | 96, 165, 250 | Secondary text accents |

### Accent Colors

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| Victory Green | `#22C55E` | 34, 197, 94 | 3-star attacks, positive stats, success |
| Bright Green | `#4ADE80` | 74, 222, 128 | Win indicators, positive deltas |
| Dark Green | `#166534` | 22, 101, 52 | Green text on dark backgrounds |
| Battle Red | `#EF4444` | 239, 68, 68 | Losses, negative stats, destruction |
| Deep Red | `#DC2626` | 220, 38, 38 | Critical warnings, missed attacks |
| Crimson | `#991B1B` | 153, 27, 27 | Dark red accents, negative borders |
| Orange | `#F97316` | 249, 115, 22 | In-progress states, elixir/resource |
| Purple | `#A855F7` | 168, 85, 247 | Rare/legendary indicators, dark elixir |
| Dark Purple | `#7C3AED` | 124, 58, 237 | League badges, special accents |

### Text Colors

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| White | `#FFFFFF` | 255, 255, 255 | Primary text on dark backgrounds |
| Off-White | `#F1F5F9` | 241, 245, 249 | Body text on dark backgrounds |
| Light Gray | `#CBD5E1` | 203, 213, 225 | Secondary/muted text |
| Medium Gray | `#94A3B8` | 148, 163, 184 | Tertiary text, labels |
| Dark Text | `#1E293B` | 30, 41, 59 | Text on light backgrounds |

### Star Rating Colors

| Stars | Color | Hex | Description |
|-------|-------|-----|-------------|
| 3 Stars | Gold | `#FFD700` | Perfect attack - bright gold stars |
| 2 Stars | Silver/Gold | `#F5C518` | Two gold stars, one empty |
| 1 Star | Silver | `#9CA3AF` | One gold star, two empty |
| 0 Stars | Gray | `#4B5563` | All empty/gray stars |

### Town Hall Level Colors (for TH badges)

| TH Range | Suggested Color | Hex | Reasoning |
|-----------|----------------|-----|-----------|
| TH 16-17 | Purple/Legendary | `#7C3AED` | Endgame content |
| TH 14-15 | Blue | `#3B82F6` | High-level |
| TH 12-13 | Teal | `#14B8A6` | Mid-high |
| TH 10-11 | Green | `#22C55E` | Midgame |
| TH 8-9 | Yellow | `#EAB308` | Early-mid |
| TH 1-7 | Gray | `#9CA3AF` | Early game |

---

## 2. Typography

### Official Clash of Clans Fonts

**Supercell Magic** is the proprietary display font used in all Supercell games.
- It is NOT freely available and cannot be used commercially.
- Characterized by: bold, rounded, slightly condensed, with a playful medieval/fantasy feel.
- Used for: game titles, major headings, button labels.

**Supercell** is the secondary brand font used for body content and UI elements in-game.

### Web-Safe Alternatives (Ranked by Similarity)

For the **display/heading** font (replacing Supercell Magic):

1. **"Lilita One"** (Google Fonts) - Best free match
   - Rounded, bold, playful, condensed display font
   - `font-family: 'Lilita One', cursive;`
   - Captures the chunky, friendly feel of CoC titles

2. **"Bungee"** (Google Fonts) - Strong alternative
   - Bold, blocky display font with a fun personality
   - `font-family: 'Bungee', cursive;`

3. **"Luckiest Guy"** (Google Fonts) - Fun/casual option
   - Very playful, comic-style display font
   - `font-family: 'Luckiest Guy', cursive;`

4. **"Titan One"** (Google Fonts) - Rounded option
   - Bold, rounded display font
   - `font-family: 'Titan One', cursive;`

5. **"Passion One"** (Google Fonts) - Clean option
   - Bold condensed with slight rounding
   - `font-family: 'Passion One', cursive;`

For the **body/UI** font:

1. **"Inter"** (Google Fonts) - Modern and highly readable
   - Clean sans-serif, excellent for data-heavy dashboards
   - `font-family: 'Inter', sans-serif;`

2. **"Nunito"** (Google Fonts) - Rounded sans-serif
   - Soft, rounded edges match the CoC aesthetic
   - `font-family: 'Nunito', sans-serif;`

3. **"Exo 2"** (Google Fonts) - Geometric/gaming feel
   - Modern geometric sans-serif with a tech/gaming feel
   - `font-family: 'Exo 2', sans-serif;`

### Recommended Font Stack

```css
:root {
  /* Display font for headings, titles, buttons */
  --font-display: 'Lilita One', 'Bungee', 'Impact', cursive;

  /* Body font for content, data, labels */
  --font-body: 'Nunito', 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

  /* Monospace for tags, codes, player tags */
  --font-mono: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
}
```

### Typography Scale

```css
:root {
  --text-xs: 0.75rem;    /* 12px - labels, captions */
  --text-sm: 0.875rem;   /* 14px - secondary text, table data */
  --text-base: 1rem;     /* 16px - body text */
  --text-lg: 1.125rem;   /* 18px - emphasized body */
  --text-xl: 1.25rem;    /* 20px - section headings */
  --text-2xl: 1.5rem;    /* 24px - card titles */
  --text-3xl: 1.875rem;  /* 30px - page headings */
  --text-4xl: 2.25rem;   /* 36px - hero headings */
  --text-5xl: 3rem;      /* 48px - splash/hero text */
}
```

### Text Styling Patterns

- **Headings**: Display font, uppercase or title case, text-shadow for depth, gold or white color
- **Body text**: Body font, normal weight (400), off-white on dark backgrounds
- **Stats/Numbers**: Body font, bold (700), larger size, white or colored by context
- **Labels**: Body font, small caps or uppercase, letter-spacing 0.05em, medium gray
- **Player Tags**: Monospace font, slightly smaller, `#` prefix always visible

```css
/* CoC-style heading with text shadow */
.coc-heading {
  font-family: var(--font-display);
  color: #F5C518;
  text-transform: uppercase;
  text-shadow:
    0 2px 0 #B8860B,
    0 4px 8px rgba(0, 0, 0, 0.5);
  letter-spacing: 0.02em;
}

/* Stat number styling */
.stat-value {
  font-family: var(--font-body);
  font-weight: 800;
  font-size: var(--text-2xl);
  color: #FFFFFF;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* Player tag styling */
.player-tag {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  color: #94A3B8;
  letter-spacing: 0.05em;
}
```

---

## 3. Gradients

### Gold Metallic Gradient (Borders, Headers)
```css
/* Horizontal gold gradient for borders/headers */
.gold-gradient {
  background: linear-gradient(
    90deg,
    #B8860B 0%,
    #DAA520 20%,
    #FFD700 40%,
    #F5C518 60%,
    #DAA520 80%,
    #B8860B 100%
  );
}

/* Vertical gold gradient for buttons */
.gold-button-gradient {
  background: linear-gradient(
    180deg,
    #FFD700 0%,
    #F5C518 40%,
    #DAA520 100%
  );
}
```

### Blue Background Gradients
```css
/* Page background gradient */
.page-gradient {
  background: linear-gradient(
    180deg,
    #0A1628 0%,
    #1A2744 50%,
    #0A1628 100%
  );
}

/* Card/panel gradient */
.panel-gradient {
  background: linear-gradient(
    180deg,
    #1E3A5F 0%,
    #1A2744 100%
  );
}

/* Darker panel variant */
.dark-panel-gradient {
  background: linear-gradient(
    135deg,
    #1A2744 0%,
    #0A1628 100%
  );
}
```

### Button Gradients
```css
/* Green action button (like "Attack" in-game) */
.green-button-gradient {
  background: linear-gradient(
    180deg,
    #4ADE80 0%,
    #22C55E 50%,
    #16A34A 100%
  );
}

/* Red/danger button */
.red-button-gradient {
  background: linear-gradient(
    180deg,
    #F87171 0%,
    #EF4444 50%,
    #DC2626 100%
  );
}

/* Blue primary button */
.blue-button-gradient {
  background: linear-gradient(
    180deg,
    #60A5FA 0%,
    #3B82F6 50%,
    #2563EB 100%
  );
}
```

### Specialty Gradients
```css
/* Leaderboard rank background (top 3) */
.rank-1-gradient { background: linear-gradient(135deg, #FFD700 0%, #F59E0B 100%); }
.rank-2-gradient { background: linear-gradient(135deg, #E5E7EB 0%, #9CA3AF 100%); }
.rank-3-gradient { background: linear-gradient(135deg, #D97706 0%, #92400E 100%); }

/* War win/loss header */
.war-win-gradient { background: linear-gradient(90deg, #166534 0%, #22C55E 50%, #166534 100%); }
.war-loss-gradient { background: linear-gradient(90deg, #991B1B 0%, #EF4444 50%, #991B1B 100%); }
```

---

## 4. Shadows & Glows

### Drop Shadows
```css
:root {
  /* Subtle elevation */
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.3), 0 1px 2px rgba(0, 0, 0, 0.2);

  /* Standard card elevation */
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.4), 0 2px 4px rgba(0, 0, 0, 0.3);

  /* Prominent elevation (modals, dropdowns) */
  --shadow-lg: 0 10px 25px rgba(0, 0, 0, 0.5), 0 4px 10px rgba(0, 0, 0, 0.4);

  /* Heavy elevation (hero cards, featured items) */
  --shadow-xl: 0 20px 40px rgba(0, 0, 0, 0.6), 0 8px 16px rgba(0, 0, 0, 0.4);
}
```

### Glow Effects
```css
/* Gold glow for featured/active items */
.gold-glow {
  box-shadow: 0 0 15px rgba(245, 197, 24, 0.4), 0 0 30px rgba(245, 197, 24, 0.2);
}

/* Green glow for success states */
.green-glow {
  box-shadow: 0 0 15px rgba(34, 197, 94, 0.4), 0 0 30px rgba(34, 197, 94, 0.2);
}

/* Red glow for alert/danger states */
.red-glow {
  box-shadow: 0 0 15px rgba(239, 68, 68, 0.4), 0 0 30px rgba(239, 68, 68, 0.2);
}

/* Blue glow for interactive elements */
.blue-glow {
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.4), 0 0 30px rgba(59, 130, 246, 0.2);
}

/* Inner glow for pressed buttons */
.inner-glow {
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.3);
}
```

### Text Shadows
```css
/* Standard text depth */
.text-shadow-sm { text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5); }

/* Prominent heading shadow */
.text-shadow-md { text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5), 0 1px 0 rgba(0, 0, 0, 0.3); }

/* Heavy display text shadow (like CoC title text) */
.text-shadow-lg {
  text-shadow:
    0 2px 0 #B8860B,
    0 4px 8px rgba(0, 0, 0, 0.5),
    0 0 20px rgba(245, 197, 24, 0.3);
}
```

---

## 5. Borders & Outlines

### Gold Border Treatment
The characteristic CoC metallic gold border used on panels, cards, and UI frames.

```css
/* Simple gold border */
.gold-border {
  border: 2px solid #DAA520;
}

/* Double gold border (more authentic) */
.gold-border-double {
  border: 3px solid #DAA520;
  outline: 1px solid #B8860B;
  outline-offset: 2px;
}

/* Gold border with inner shadow for depth */
.gold-border-3d {
  border: 2px solid #DAA520;
  box-shadow:
    inset 0 1px 0 rgba(255, 215, 0, 0.3),
    0 1px 0 #B8860B,
    0 2px 4px rgba(0, 0, 0, 0.3);
}

/* Metallic gold border using gradient */
.gold-border-gradient {
  border: 3px solid transparent;
  background-clip: padding-box;
  position: relative;
}
.gold-border-gradient::before {
  content: '';
  position: absolute;
  inset: -3px;
  border-radius: inherit;
  background: linear-gradient(135deg, #B8860B, #FFD700, #DAA520, #B8860B);
  z-index: -1;
}
```

### Dividers & Separators
```css
/* Horizontal gold divider */
.gold-divider {
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    #DAA520 20%,
    #FFD700 50%,
    #DAA520 80%,
    transparent 100%
  );
}

/* Section separator with ornament */
.ornate-divider {
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    #DAA520 30%,
    transparent 50%,
    #DAA520 70%,
    transparent 100%
  );
  position: relative;
}
.ornate-divider::after {
  content: '\2756'; /* diamond ornament */
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: #FFD700;
  font-size: 1rem;
  background: var(--bg-primary);
  padding: 0 0.5rem;
}
```

### Border Radius Values
```css
:root {
  --radius-sm: 4px;    /* Small elements, badges */
  --radius-md: 8px;    /* Buttons, inputs */
  --radius-lg: 12px;   /* Cards, panels */
  --radius-xl: 16px;   /* Large panels, modals */
  --radius-2xl: 24px;  /* Pill shapes, tags */
  --radius-full: 9999px; /* Circular elements */
}
```

---

## 6. Shapes & Layout

### Layout Grid
```css
/* Dashboard layout */
.dashboard-layout {
  display: grid;
  grid-template-columns: 280px 1fr;  /* Sidebar + Content */
  grid-template-rows: 64px 1fr;       /* Header + Content */
  min-height: 100vh;
  background: #0A1628;
}

/* Content grid for cards */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  padding: 1.5rem;
}

/* Leaderboard table layout */
.leaderboard-layout {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem;
}
```

### Spacing Scale
```css
:root {
  --space-1: 0.25rem;   /* 4px */
  --space-2: 0.5rem;    /* 8px */
  --space-3: 0.75rem;   /* 12px */
  --space-4: 1rem;      /* 16px */
  --space-5: 1.25rem;   /* 20px */
  --space-6: 1.5rem;    /* 24px */
  --space-8: 2rem;      /* 32px */
  --space-10: 2.5rem;   /* 40px */
  --space-12: 3rem;     /* 48px */
  --space-16: 4rem;     /* 64px */
}
```

### Key Shape Patterns

**Shield/Crest Shape** - For clan badges and league icons:
```css
.shield-shape {
  clip-path: polygon(
    0% 0%, 100% 0%, 100% 70%,
    50% 100%, 0% 70%
  );
}
```

**Banner/Ribbon Shape** - For section headers and labels:
```css
.ribbon {
  position: relative;
  display: inline-block;
  padding: 0.5rem 2rem;
  background: linear-gradient(180deg, #DAA520 0%, #B8860B 100%);
  color: white;
  font-family: var(--font-display);
  text-transform: uppercase;
}
.ribbon::before,
.ribbon::after {
  content: '';
  position: absolute;
  bottom: -8px;
  border: 8px solid transparent;
}
.ribbon::before {
  left: 0;
  border-right-color: #8B6914;
  border-top-color: #8B6914;
}
.ribbon::after {
  right: 0;
  border-left-color: #8B6914;
  border-top-color: #8B6914;
}
```

**Hexagonal Badge** - For rank/level indicators:
```css
.hex-badge {
  clip-path: polygon(
    50% 0%, 100% 25%, 100% 75%,
    50% 100%, 0% 75%, 0% 25%
  );
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}
```

---

## 7. Buttons

### Primary Action Button (Gold)
```css
.btn-primary {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #1E293B;
  padding: 0.75rem 2rem;
  border: 2px solid #B8860B;
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, #FFD700 0%, #F5C518 40%, #DAA520 100%);
  box-shadow:
    0 2px 0 #8B6914,
    0 4px 8px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all 0.15s ease;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.3);
}

.btn-primary:hover {
  background: linear-gradient(180deg, #FFE44D 0%, #FFD700 40%, #F5C518 100%);
  box-shadow:
    0 2px 0 #8B6914,
    0 6px 12px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(245, 197, 24, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  transform: translateY(-1px);
}

.btn-primary:active {
  background: linear-gradient(180deg, #DAA520 0%, #B8860B 100%);
  box-shadow:
    0 1px 0 #8B6914,
    inset 0 2px 4px rgba(0, 0, 0, 0.3);
  transform: translateY(1px);
}
```

### Secondary Button (Blue)
```css
.btn-secondary {
  font-family: var(--font-body);
  font-weight: 700;
  font-size: var(--text-base);
  color: #FFFFFF;
  padding: 0.625rem 1.5rem;
  border: 2px solid #3B82F6;
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, #3B82F6 0%, #2563EB 100%);
  box-shadow:
    0 2px 0 #1D4ED8,
    0 4px 8px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-secondary:hover {
  background: linear-gradient(180deg, #60A5FA 0%, #3B82F6 100%);
  box-shadow:
    0 2px 0 #1D4ED8,
    0 6px 12px rgba(0, 0, 0, 0.4),
    0 0 15px rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}
```

### Success/Action Button (Green)
```css
.btn-success {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  text-transform: uppercase;
  color: #FFFFFF;
  padding: 0.75rem 2rem;
  border: 2px solid #16A34A;
  border-radius: var(--radius-md);
  background: linear-gradient(180deg, #4ADE80 0%, #22C55E 50%, #16A34A 100%);
  box-shadow:
    0 2px 0 #15803D,
    0 4px 8px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}
```

### Ghost/Outline Button
```css
.btn-ghost {
  font-family: var(--font-body);
  font-weight: 600;
  color: #DAA520;
  padding: 0.625rem 1.5rem;
  border: 2px solid #DAA520;
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-ghost:hover {
  background: rgba(218, 165, 32, 0.1);
  box-shadow: 0 0 15px rgba(218, 165, 32, 0.2);
}
```

### Small/Tag Button
```css
.btn-sm {
  font-family: var(--font-body);
  font-weight: 600;
  font-size: var(--text-sm);
  color: #F1F5F9;
  padding: 0.25rem 0.75rem;
  border: 1px solid #2B4A7A;
  border-radius: var(--radius-2xl);
  background: #1E3A5F;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-sm:hover {
  background: #2B4A7A;
  border-color: #3B82F6;
}
```

---

## 8. Cards & Containers

### Standard Panel/Card
```css
.coc-card {
  background: linear-gradient(180deg, #1E3A5F 0%, #1A2744 100%);
  border: 2px solid #2B4A7A;
  border-radius: var(--radius-lg);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.4), 0 2px 4px rgba(0, 0, 0, 0.3);
  overflow: hidden;
}

.coc-card-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #2B4A7A;
  background: rgba(0, 0, 0, 0.2);
}

.coc-card-header h3 {
  font-family: var(--font-display);
  color: #F5C518;
  font-size: var(--text-xl);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin: 0;
}

.coc-card-body {
  padding: 1.25rem;
}
```

### Featured/Gold-Border Card
```css
.coc-card-featured {
  background: linear-gradient(180deg, #1E3A5F 0%, #1A2744 100%);
  border: 2px solid #DAA520;
  border-radius: var(--radius-lg);
  box-shadow:
    0 4px 6px rgba(0, 0, 0, 0.4),
    0 0 15px rgba(218, 165, 32, 0.15);
  overflow: hidden;
}

.coc-card-featured .coc-card-header {
  background: linear-gradient(90deg, #B8860B 0%, #DAA520 50%, #B8860B 100%);
  border-bottom: none;
}

.coc-card-featured .coc-card-header h3 {
  color: #FFFFFF;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}
```

### Stat Card (Small)
```css
.stat-card {
  background: rgba(30, 58, 95, 0.5);
  border: 1px solid #2B4A7A;
  border-radius: var(--radius-md);
  padding: 1rem;
  text-align: center;
}

.stat-card .stat-label {
  font-family: var(--font-body);
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #94A3B8;
  margin-bottom: 0.25rem;
}

.stat-card .stat-value {
  font-family: var(--font-body);
  font-weight: 800;
  font-size: var(--text-2xl);
  color: #FFFFFF;
}
```

### Player/Member Row
```css
.member-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid rgba(43, 74, 122, 0.5);
  transition: background 0.15s ease;
}

.member-row:hover {
  background: rgba(59, 130, 246, 0.08);
}

.member-row .rank {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  color: #DAA520;
  min-width: 2rem;
  text-align: center;
}

.member-row .player-name {
  font-family: var(--font-body);
  font-weight: 700;
  color: #FFFFFF;
  flex: 1;
}

.member-row .player-tag {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  color: #64748B;
}
```

---

## 9. Navigation

### Top Navigation Bar
```css
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 1.5rem;
  background: rgba(10, 22, 40, 0.95);
  border-bottom: 2px solid #DAA520;
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar .logo {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  color: #F5C518;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.nav-link {
  font-family: var(--font-body);
  font-weight: 600;
  font-size: var(--text-sm);
  color: #CBD5E1;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  transition: all 0.15s ease;
  text-decoration: none;
}

.nav-link:hover {
  color: #F5C518;
  background: rgba(245, 197, 24, 0.08);
}

.nav-link.active {
  color: #F5C518;
  background: rgba(245, 197, 24, 0.12);
  border-bottom: 2px solid #F5C518;
}
```

### Sidebar Navigation
```css
.sidebar {
  width: 280px;
  background: #0F1D32;
  border-right: 1px solid #1E3A5F;
  padding: 1rem 0;
  overflow-y: auto;
}

.sidebar-section {
  padding: 0 0.75rem;
  margin-bottom: 1.5rem;
}

.sidebar-section-label {
  font-family: var(--font-body);
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #64748B;
  padding: 0 0.75rem;
  margin-bottom: 0.5rem;
}

.sidebar-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 0.75rem;
  border-radius: var(--radius-md);
  color: #CBD5E1;
  font-weight: 500;
  transition: all 0.15s ease;
  text-decoration: none;
}

.sidebar-link:hover {
  background: rgba(59, 130, 246, 0.08);
  color: #F1F5F9;
}

.sidebar-link.active {
  background: rgba(245, 197, 24, 0.12);
  color: #F5C518;
  border-left: 3px solid #F5C518;
}
```

### Tab Navigation (for switching views: Leaderboard / Wars / Participants)
```css
.tab-nav {
  display: flex;
  gap: 0.25rem;
  padding: 0.25rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: var(--radius-lg);
}

.tab-button {
  font-family: var(--font-body);
  font-weight: 600;
  font-size: var(--text-sm);
  color: #94A3B8;
  padding: 0.5rem 1.25rem;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-button:hover {
  color: #F1F5F9;
  background: rgba(255, 255, 255, 0.05);
}

.tab-button.active {
  color: #F5C518;
  background: #1E3A5F;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}
```

---

## 10. Icons & Imagery

### Star Icons (Attack Stars)
Stars are the most critical icon in a CWL tracker. In CoC, attack results are shown as 1-3 gold stars.

```css
/* Star rating display */
.stars {
  display: flex;
  gap: 2px;
}

.star {
  width: 20px;
  height: 20px;
}

.star-earned {
  color: #FFD700;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.star-empty {
  color: #374151;
}

/* CSS-only star using Unicode */
.star::before {
  content: '\2605'; /* Filled star */
  font-size: 1.25rem;
}

/* Compact star count (e.g., "3/3") */
.star-count {
  font-family: var(--font-body);
  font-weight: 700;
  color: #FFD700;
}
.star-count .max {
  color: #64748B;
  font-weight: 400;
}
```

### Recommended Icon Libraries

1. **Lucide Icons** (https://lucide.dev) - Best for dashboards
   - Clean, consistent line icons
   - Good coverage of: swords, shield, trophy, crown, star, users, settings
   - Tree-shakeable, React/Vue/Svelte components available
   - MIT license

2. **Heroicons** (https://heroicons.com) - Simple and modern
   - Made by Tailwind Labs
   - Outline and solid variants
   - MIT license

3. **Tabler Icons** (https://tabler.io/icons) - Comprehensive
   - 5000+ icons including sword, shield, crown, trophy
   - MIT license

4. **Game-icons.net** (https://game-icons.net) - Game-specific
   - Massive library of game-themed icons
   - Includes: sword, shield, castle, crown, skull, potion
   - CC BY 3.0 license
   - SVG format, can be styled with CSS

### Key Icons Needed

| Concept | Unicode | Suggested Lucide Icon |
|---------|---------|----------------------|
| Attack star | `\2605` | `star` |
| Sword/Attack | `\2694` | `swords` |
| Shield/Defense | `\1F6E1` | `shield` |
| Trophy | `\1F3C6` | `trophy` |
| Crown (Leader) | `\1F451` | `crown` |
| Clan | - | `users` |
| War | - | `swords` |
| Player | - | `user` |
| Settings | - | `settings` |
| Sync/Refresh | - | `refresh-cw` |
| Search | - | `search` |
| Calendar (Season) | - | `calendar` |
| Chart (Stats) | - | `bar-chart-3` |
| Rank Up | - | `trending-up` |
| Rank Down | - | `trending-down` |
| Town Hall | - | `castle` (from game-icons) |

### Clan Badge Display
CoC provides clan badge URLs via the API. Display them in circular frames with gold borders.

```css
.clan-badge {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-full);
  border: 2px solid #DAA520;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  object-fit: cover;
  background: #1A2744;
}

.clan-badge-lg {
  width: 80px;
  height: 80px;
  border: 3px solid #DAA520;
}
```

---

## 11. Textures & Backgrounds

### Background Patterns
CoC uses rich, textured backgrounds. For a web dashboard, use subtle CSS patterns to evoke this without heavy images.

```css
/* Subtle grid pattern overlay */
.bg-grid {
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.03) 1px, transparent 1px);
  background-size: 32px 32px;
}

/* Subtle noise texture (use as overlay) */
.bg-noise {
  position: relative;
}
.bg-noise::after {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  pointer-events: none;
}

/* Diagonal stripes (subtle) */
.bg-stripes {
  background-image: repeating-linear-gradient(
    45deg,
    transparent,
    transparent 10px,
    rgba(255, 255, 255, 0.01) 10px,
    rgba(255, 255, 255, 0.01) 20px
  );
}

/* Vignette overlay for depth */
.bg-vignette {
  background: radial-gradient(
    ellipse at center,
    transparent 40%,
    rgba(0, 0, 0, 0.4) 100%
  );
}
```

### Page Background Composition
```css
body {
  background-color: #0A1628;
  background-image:
    radial-gradient(ellipse at 20% 50%, rgba(30, 58, 95, 0.4) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 20%, rgba(43, 74, 122, 0.3) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 100%, rgba(30, 58, 95, 0.3) 0%, transparent 40%);
  color: #F1F5F9;
  font-family: var(--font-body);
  min-height: 100vh;
}
```

---

## 12. In-Game UI Components

### War Map / Battle Layout
The CWL war map in-game shows two clans side by side with members ordered by map position (1 = top/strongest). Key visual elements:
- Members displayed as rows with TH icon, player name, and position number
- Attacks shown as connecting lines between attacker and defender
- Stars displayed next to each defense/attack result
- War status bar at top showing total stars and destruction %

**Web adaptation**: A responsive table or list view with position numbers, TH badges, and star ratings inline.

### Leaderboard/Ranking Screen
In-game leaderboards feature:
- Numbered ranks with top 3 getting special gold/silver/bronze treatment
- Player name and clan badge side by side
- Trophy/score count on the right side
- Alternating row backgrounds for readability
- Smooth scroll with a slight blue/dark gradient

```css
/* Leaderboard row styling */
.leaderboard-row {
  display: grid;
  grid-template-columns: 48px 48px 1fr auto;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid rgba(43, 74, 122, 0.3);
  transition: background 0.15s ease;
}

.leaderboard-row:nth-child(even) {
  background: rgba(0, 0, 0, 0.1);
}

.leaderboard-row:hover {
  background: rgba(59, 130, 246, 0.08);
}

/* Top 3 special styling */
.leaderboard-row.rank-1 {
  background: rgba(255, 215, 0, 0.08);
  border-left: 3px solid #FFD700;
}
.leaderboard-row.rank-2 {
  background: rgba(192, 192, 192, 0.05);
  border-left: 3px solid #C0C0C0;
}
.leaderboard-row.rank-3 {
  background: rgba(205, 127, 50, 0.05);
  border-left: 3px solid #CD7F32;
}
```

### Player Profile Screen
In-game profiles show:
- Large hero portrait area at top
- Player name with level badge
- Clan name and role underneath
- Stats grid: trophies, war stars, donations
- Troop/hero/spell levels in a scrollable grid
- Achievement badges

### Clan Info Screen
- Clan badge (large, centered)
- Clan name and tag
- War log stats (wins/losses/draws)
- Member list with roles, trophies, donations
- Clan description text

---

## 13. CWL-Specific UI Elements

### Season Selector
```css
.season-selector {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.season-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: rgba(30, 58, 95, 0.5);
  border: 1px solid #2B4A7A;
  border-radius: var(--radius-2xl);
  color: #F1F5F9;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.season-badge:hover {
  border-color: #F5C518;
  background: rgba(245, 197, 24, 0.08);
}

.season-badge.active {
  border-color: #F5C518;
  background: rgba(245, 197, 24, 0.12);
  color: #F5C518;
}
```

### War Day Card
```css
.war-day-card {
  background: linear-gradient(180deg, #1E3A5F 0%, #1A2744 100%);
  border: 1px solid #2B4A7A;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.war-day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid #2B4A7A;
}

.war-day-title {
  font-family: var(--font-display);
  color: #F5C518;
  font-size: var(--text-lg);
}

.war-day-status {
  font-size: var(--text-sm);
  font-weight: 600;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-2xl);
}

.war-day-status.win {
  background: rgba(34, 197, 94, 0.15);
  color: #4ADE80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.war-day-status.loss {
  background: rgba(239, 68, 68, 0.15);
  color: #F87171;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.war-matchup {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 1rem;
  gap: 1rem;
}

.war-clan {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.war-score {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  color: #FFFFFF;
}

.war-vs {
  font-family: var(--font-display);
  color: #64748B;
  font-size: var(--text-lg);
}
```

### Attack Detail Row
```css
.attack-row {
  display: grid;
  grid-template-columns: 36px 1fr auto 60px auto 1fr 36px;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-bottom: 1px solid rgba(43, 74, 122, 0.2);
  font-size: var(--text-sm);
}

.attack-row .position {
  font-family: var(--font-body);
  font-weight: 700;
  color: #94A3B8;
  text-align: center;
}

.attack-row .attacker-name {
  color: #F1F5F9;
  font-weight: 600;
  text-align: right;
}

.attack-row .stars-display {
  display: flex;
  gap: 2px;
  justify-content: center;
}

.attack-row .destruction {
  font-weight: 600;
  text-align: center;
}

.attack-row .defender-name {
  color: #F1F5F9;
  font-weight: 600;
}

.attack-row .arrow {
  color: #F5C518;
  font-size: var(--text-lg);
  text-align: center;
}
```

### CBDS Score Badge
Custom component for displaying the CBDS calculated score.

```css
.cbds-score {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  height: 32px;
  padding: 0 0.5rem;
  border-radius: var(--radius-md);
  font-family: var(--font-body);
  font-weight: 800;
  font-size: var(--text-base);
}

/* Score color ranges */
.cbds-score.excellent { /* 100+ */
  background: rgba(34, 197, 94, 0.15);
  color: #4ADE80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.cbds-score.good { /* 60-99 */
  background: rgba(59, 130, 246, 0.15);
  color: #60A5FA;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.cbds-score.average { /* 30-59 */
  background: rgba(245, 158, 11, 0.15);
  color: #FBBF24;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.cbds-score.poor { /* 0-29 */
  background: rgba(239, 68, 68, 0.15);
  color: #F87171;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.cbds-score.negative { /* < 0 */
  background: rgba(239, 68, 68, 0.25);
  color: #FCA5A5;
  border: 1px solid rgba(239, 68, 68, 0.5);
}
```

### TH Level Badge
```css
.th-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  font-family: var(--font-body);
  font-weight: 800;
  font-size: var(--text-xs);
  border: 1px solid;
}

.th-badge.th-16, .th-badge.th-17 {
  background: rgba(124, 58, 237, 0.2);
  color: #A78BFA;
  border-color: rgba(124, 58, 237, 0.4);
}

.th-badge.th-14, .th-badge.th-15 {
  background: rgba(59, 130, 246, 0.2);
  color: #60A5FA;
  border-color: rgba(59, 130, 246, 0.4);
}

.th-badge.th-12, .th-badge.th-13 {
  background: rgba(20, 184, 166, 0.2);
  color: #5EEAD4;
  border-color: rgba(20, 184, 166, 0.4);
}

.th-badge.th-10, .th-badge.th-11 {
  background: rgba(34, 197, 94, 0.2);
  color: #4ADE80;
  border-color: rgba(34, 197, 94, 0.4);
}

.th-badge.th-low {
  background: rgba(156, 163, 175, 0.2);
  color: #D1D5DB;
  border-color: rgba(156, 163, 175, 0.4);
}
```

---

## 14. Web Dashboard Adaptation Strategy

### Design Philosophy
The goal is NOT to make a pixel-perfect replica of the CoC game UI. Instead, create a **modern web dashboard** that **evokes** the Clash of Clans aesthetic through:

1. **Color palette** - Use the deep navy backgrounds with gold accents
2. **Typography** - Use a fun display font for headings but clean sans-serif for data
3. **Depth** - Liberal use of shadows and subtle gradients
4. **Gold accents** - Borders, dividers, and interactive elements in gold
5. **Dark theme** - The entire app should be dark-themed like the game
6. **Stars** - Star icons are the primary visual motif

### Key Principles

1. **Readability first**: This is a data-heavy dashboard. Never sacrifice readability for decoration.
2. **Responsive**: Must work on desktop (primary) and tablet. Mobile is nice-to-have.
3. **Performance**: Avoid heavy textures or images. Use CSS gradients and SVG icons.
4. **Accessible**: Gold on dark blue meets WCAG AA contrast ratios. Test all combinations.
5. **Subtle theming**: Don't make it look like a children's game. Keep it mature/sporty.

### Page Layout Recommendations

**Dashboard/Home**
- Top: Navbar with CBDS logo, clan selector, sync button
- Left: Sidebar with navigation (Leaderboard, Wars, Players, Settings)
- Main: Season overview cards, quick stats grid, recent war results

**Leaderboard Page**
- Season selector at top
- Full leaderboard table with rank, name, TH badge, CBDS score, stars, attacks
- Click-to-expand row showing per-war-day breakdown
- Score breakdown tooltip on hover

**War Detail Page**
- War header: Clan A vs Clan B with badges and total stars
- Members list: position, TH badge, name, attacks made/received
- Attack flow: visual representation of who attacked whom

**Player Profile Page**
- Player header: name, tag, TH level, role
- CWL performance chart: CBDS score over seasons
- Per-war breakdown table
- Score components pie/bar chart

### Technology Recommendations for Frontend

1. **React + TypeScript** - Component-based, type-safe
2. **Tailwind CSS** - Utility-first, easy to customize with CoC colors
3. **Framer Motion** - Smooth animations (star reveals, score counters)
4. **Recharts or Chart.js** - For performance charts
5. **Lucide React** - Icon library
6. **Tanstack Query** - API data fetching and caching

### Tailwind CSS Configuration

```js
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        // CoC Brand Colors
        'coc-gold': {
          50: '#FFFDF0',
          100: '#FFEAA7',
          200: '#FFD700',
          300: '#F5C518',
          400: '#DAA520',
          500: '#B8860B',
          600: '#8B6914',
          700: '#6B5210',
          800: '#4A3A0C',
          900: '#2A2008',
        },
        // Background Colors
        'coc-navy': {
          50: '#2B4A7A',
          100: '#1E3A5F',
          200: '#1A2744',
          300: '#152036',
          400: '#0F1D32',
          500: '#0A1628',
          600: '#070F1C',
          700: '#050B14',
        },
        // Semantic Colors
        'coc-win': '#22C55E',
        'coc-loss': '#EF4444',
        'coc-draw': '#F59E0B',
        'coc-star': '#FFD700',
      },
      fontFamily: {
        'display': ['"Lilita One"', '"Bungee"', 'Impact', 'cursive'],
        'body': ['"Nunito"', '"Inter"', '-apple-system', 'sans-serif'],
        'mono': ['"JetBrains Mono"', '"Fira Code"', 'monospace'],
      },
      boxShadow: {
        'coc': '0 4px 6px rgba(0,0,0,0.4), 0 2px 4px rgba(0,0,0,0.3)',
        'coc-lg': '0 10px 25px rgba(0,0,0,0.5), 0 4px 10px rgba(0,0,0,0.4)',
        'gold-glow': '0 0 15px rgba(245,197,24,0.4), 0 0 30px rgba(245,197,24,0.2)',
        'green-glow': '0 0 15px rgba(34,197,94,0.4)',
        'red-glow': '0 0 15px rgba(239,68,68,0.4)',
      },
      borderRadius: {
        'coc': '12px',
      },
    },
  },
}
```

---

## 15. CSS Component Library

### Complete CSS Custom Properties (Copy-Paste Ready)

```css
:root {
  /* === COLORS === */
  /* Gold */
  --gold-50: #FFFDF0;
  --gold-100: #FFEAA7;
  --gold-200: #FFD700;
  --gold-300: #F5C518;
  --gold-400: #DAA520;
  --gold-500: #B8860B;
  --gold-600: #8B6914;

  /* Navy/Background */
  --navy-50: #2B4A7A;
  --navy-100: #1E3A5F;
  --navy-200: #1A2744;
  --navy-300: #152036;
  --navy-400: #0F1D32;
  --navy-500: #0A1628;

  /* Semantic */
  --color-success: #22C55E;
  --color-success-light: #4ADE80;
  --color-danger: #EF4444;
  --color-danger-light: #F87171;
  --color-warning: #F59E0B;
  --color-warning-light: #FBBF24;
  --color-info: #3B82F6;
  --color-info-light: #60A5FA;
  --color-star: #FFD700;

  /* Text */
  --text-primary: #F1F5F9;
  --text-secondary: #CBD5E1;
  --text-muted: #94A3B8;
  --text-dim: #64748B;
  --text-dark: #1E293B;

  /* === TYPOGRAPHY === */
  --font-display: 'Lilita One', 'Bungee', 'Impact', cursive;
  --font-body: 'Nunito', 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;

  --text-xs: 0.75rem;
  --text-sm: 0.875rem;
  --text-base: 1rem;
  --text-lg: 1.125rem;
  --text-xl: 1.25rem;
  --text-2xl: 1.5rem;
  --text-3xl: 1.875rem;
  --text-4xl: 2.25rem;

  /* === SPACING === */
  --space-1: 0.25rem;
  --space-2: 0.5rem;
  --space-3: 0.75rem;
  --space-4: 1rem;
  --space-6: 1.5rem;
  --space-8: 2rem;
  --space-12: 3rem;
  --space-16: 4rem;

  /* === BORDERS === */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-2xl: 24px;
  --radius-full: 9999px;

  --border-subtle: 1px solid #2B4A7A;
  --border-gold: 2px solid #DAA520;
  --border-strong: 2px solid #3B82F6;

  /* === SHADOWS === */
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.3), 0 1px 2px rgba(0,0,0,0.2);
  --shadow-md: 0 4px 6px rgba(0,0,0,0.4), 0 2px 4px rgba(0,0,0,0.3);
  --shadow-lg: 0 10px 25px rgba(0,0,0,0.5), 0 4px 10px rgba(0,0,0,0.4);
  --shadow-xl: 0 20px 40px rgba(0,0,0,0.6), 0 8px 16px rgba(0,0,0,0.4);

  /* === TRANSITIONS === */
  --transition-fast: 0.1s ease;
  --transition-base: 0.15s ease;
  --transition-slow: 0.3s ease;
}
```

### Animation Keyframes
```css
/* Star reveal animation (for when attack results load) */
@keyframes star-pop {
  0% { transform: scale(0); opacity: 0; }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); opacity: 1; }
}

.star-animate {
  animation: star-pop 0.3s ease-out forwards;
}
.star-animate:nth-child(2) { animation-delay: 0.15s; }
.star-animate:nth-child(3) { animation-delay: 0.3s; }

/* Score counter animation */
@keyframes count-up {
  0% { opacity: 0; transform: translateY(10px); }
  100% { opacity: 1; transform: translateY(0); }
}

.score-animate {
  animation: count-up 0.5s ease-out;
}

/* Shimmer loading effect */
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.loading-shimmer {
  background: linear-gradient(
    90deg,
    #1A2744 25%,
    #2B4A7A 50%,
    #1A2744 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-md);
}

/* Pulse glow for live/syncing indicators */
@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 5px rgba(34, 197, 94, 0.4); }
  50% { box-shadow: 0 0 15px rgba(34, 197, 94, 0.6), 0 0 30px rgba(34, 197, 94, 0.3); }
}

.sync-indicator {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--color-success);
  animation: pulse-glow 2s infinite;
}

/* Slide-in for card/row animations */
@keyframes slide-in {
  0% { opacity: 0; transform: translateX(-20px); }
  100% { opacity: 1; transform: translateX(0); }
}

.row-animate {
  animation: slide-in 0.3s ease-out forwards;
}
```

---

## 16. External Resources & Assets

### Free Font Sources (Google Fonts)
- **Lilita One**: https://fonts.google.com/specimen/Lilita+One
- **Bungee**: https://fonts.google.com/specimen/Bungee
- **Luckiest Guy**: https://fonts.google.com/specimen/Luckiest+Guy
- **Titan One**: https://fonts.google.com/specimen/Titan+One
- **Nunito**: https://fonts.google.com/specimen/Nunito
- **Inter**: https://fonts.google.com/specimen/Inter
- **JetBrains Mono**: https://fonts.google.com/specimen/JetBrains+Mono

### Icon Libraries
- **Lucide**: https://lucide.dev (MIT, React components)
- **Heroicons**: https://heroicons.com (MIT, by Tailwind Labs)
- **Tabler Icons**: https://tabler.io/icons (MIT, 5000+ icons)
- **Game-icons.net**: https://game-icons.net (CC BY 3.0, fantasy/game themed)

### CoC API Assets
The Clash of Clans API provides URLs for:
- Clan badge images (small, medium, large) via `badgeUrls` in clan responses
- League icons via league response data
- These can be displayed directly -- they are hosted by Supercell CDN

### Design Inspiration & Reference
- **Supercell Fan Kit**: Supercell occasionally provides fan content kits (logos, badges) for community use. Check https://supercell.com/en/fan-content-policy/ for current terms.
- **Clash of Stats** (https://www.clashofstats.com): Fan-made stats tracker with a CoC-inspired dark theme. Good reference for dashboard layout.
- **Clash Ninja** (https://www.clash.ninja): Another fan tracker with progress tracking UI. Clean, dark design.
- **ClashPerk** (https://clashperk.com): Discord bot with web dashboard. Modern dark theme.
- **Clash King**: Stats platform with clean data presentation.

### CSS Framework Recommendations
- **Tailwind CSS** (https://tailwindcss.com): Best choice for custom theming. The config above sets up all CoC colors.
- **shadcn/ui** (https://ui.shadcn.com): Unstyled, composable components that work with Tailwind. Can be themed to match CoC palette.
- **Radix UI** (https://www.radix-ui.com): Accessible primitives for dropdowns, modals, tooltips.

### Chart Libraries for Stats
- **Recharts** (https://recharts.org): React-based, easy to theme, good for bar/line charts
- **Chart.js** (https://www.chartjs.org): Lightweight, canvas-based, good for radar charts
- **Nivo** (https://nivo.rocks): Beautiful, themed charts with built-in dark mode

### Additional Design Toolkit
- **Coolors** (https://coolors.co): Generate complementary color palettes from the CoC golds/blues
- **Realtime Colors** (https://www.realtimecolors.com): Preview color schemes live
- **Type Scale** (https://typescale.com): Generate consistent type hierarchies
- **CSS Gradient** (https://cssgradient.io): Fine-tune gradient values visually

---

## Appendix: Quick Reference Card

### At-a-Glance Color Guide
```
Background:     #0A1628 (deep navy)
Panel:          #1A2744 (dark blue)
Card:           #1E3A5F (mid blue)
Border:         #2B4A7A (blue-gray)
Gold accent:    #DAA520 (rich gold)
Gold highlight: #FFD700 (bright gold)
Text primary:   #F1F5F9 (off-white)
Text secondary: #94A3B8 (gray)
Success:        #22C55E (green)
Danger:         #EF4444 (red)
Star:           #FFD700 (gold)
```

### Quick Font Stack
```
Headings: 'Lilita One', cursive
Body:     'Nunito', sans-serif
Code:     'JetBrains Mono', monospace
```

### Design Mantras
1. Dark navy backgrounds, gold accents, white text
2. Depth through shadows and gradients, not flat
3. Stars are the primary visual motif
4. Data readability above decoration
5. Modern dashboard feel, medieval flavor via color and type
