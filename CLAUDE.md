# Architecture Guidelines

## Mobile App Structure (React Native)

```
App.tsx              # Entry point
src/
├── components/     # Reusable UI components
├── screens/        # Screen components
├── services/       # API, business logic
├── hooks/          # Custom React hooks
├── utils/          # Helper functions
└── types/          # TypeScript types
```

## Dependency Rules

- Components should only use hooks and utils
- Screens can use components, hooks, services
- Services handle business logic and API calls
- NO direct API calls in components (use services)

## Naming Conventions

- Components: `*.component.tsx` or `*.tsx`
- Screens: `*.screen.tsx`
- Hooks: `use*.ts` or `*.hook.ts`
- Services: `*.service.ts`

## Forbidden Patterns

- NO business logic in components
- NO direct fetch/axios calls in components
- All API calls through services
- Constants in separate files, not inline

## Before Generating Code

1. Identify if creating component, screen, or service
2. Follow naming conventions
3. Keep components small and focused
4. Run: `npm run lint` before commit