# مانیتورینگ تسک‌ها — React Port

React/JSX port of the uploaded `index.html` task-monitoring dashboard. Markup, class names, and CSS are preserved 1:1 from the original so the rendered output is visually identical; all vanilla-JS DOM manipulation was converted to React state/props.

## Component hierarchy

```
App
├── BackgroundGlow                (decorative fixed-position glow circles)
├── Sidebar                       (nav + user card)
│     — reads navSections from data/dashboardData.js
│     — owns no state itself; active item + open/close controlled by App
└── main.main-content
    ├── TopHeader                 (title, breadcrumb, search box, header buttons, mobile menu toggle)
    ├── StatsGrid                 (renders 4x StatCard)
    │     └── StatCard            (icon, trend, value, label)
    └── section.content-grid
        ├── TaskTable             (filter buttons + table)
        │     └── TaskRow (xN)   (one per visible task)
        └── div.side-panels
            ├── DonutChart        (canvas chart + legend)
            └── ActivityList      (recent activity feed)
```

## State (owned by `App`)

| State             | Purpose                                                            |
|-------------------|---------------------------------------------------------------------|
| `activeNavKey`    | Which sidebar nav item is highlighted (`"sectionIdx-itemIdx"`)      |
| `activeFilter`    | Current status filter for the task table (`all/completed/...`)     |
| `searchQuery`     | Current text in the search box                                     |
| `displaySource`   | `'filter'` or `'search'` — mirrors the original's behavior where typing in search overrides the status filter until the input is cleared, and clicking a filter button reverts to filter-based display |
| `isMobile`        | Tracks `window.innerWidth <= 768`, toggled via a `resize` listener  |
| `sidebarOpen`     | Whether the mobile sidebar is slid into view                       |

All task/activity/label data lives in `src/data/dashboardData.js`, and the two small pure helpers (`toPersianNum`, `getProgressColor`) live in `src/utils/helpers.js` — matching the original script's data/logic split.

## Notable behavior preserved from the original

- **Search vs. filter precedence**: typing a query filters by name/description/assignee/ID and ignores the active status filter; clearing the search box resets to the "همه" (all) filter — exactly as the original `input` listener did.
- **Donut chart**: drawn on a `<canvas>` via the same trigonometry (start angle, gaps between segments, inner/outer radius) inside a `useEffect` that runs once on mount, with device-pixel-ratio scaling.
- **Mobile sidebar**: hidden off-canvas via `transform: translateX(100%)` under 768px, toggled by the hamburger button which only renders/displays on mobile — same as the original `checkMobile()`/`menuToggle` logic.
- **RTL/Persian**: `public/index.html` keeps `lang="fa" dir="rtl"` and the same three CDN stylesheets (Tailwind reset, Vazirmatn font, Font Awesome) the original used.

## Running it

```bash
npm install
npm start
```

(Built with Create React App's `react-scripts`; swap in Vite or any other bundler if preferred — no CRA-specific APIs are used beyond the standard `public/index.html` + `src/index.js` entry point.)
