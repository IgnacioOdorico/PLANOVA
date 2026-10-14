# Feature: One-command startup (Docker Compose + INICIAR.bat)

## Objective
Let any user start the full Planova stack (MySQL + backend + frontend) with one command (`docker compose up`) or a double-click (`INICIAR.bat`), and document it explicitly in the README.

## Problem / Why
Starting the project today requires a local JDK 17, Maven, MySQL 8 and Node, plus manually exporting `.env` values. On the maintainer's machine the JDK was missing and port conflicts / the `DotEnvConfig` loader (which reads `.env` from the working directory and overrides real env vars) broke container startup.

## Scope
- Backend Dockerfile (multi-stage Maven -> JRE 17), `.dockerignore` (never ship `.env`).
- Frontend Dockerfile (Vite build -> nginx), nginx config with SPA fallback and `/api/` proxy that strips the `/api` prefix (same as `vite.config.ts`).
- Root `docker-compose.yml` (mysql, backend, frontend), root `.env.example`, root `.gitignore` covering `.env`.
- `INICIAR.bat` (checks Docker, creates `.env` from example if missing, runs compose, opens browser) and `DETENER.bat`.
- README "Quick start" rewritten: Docker path first, manual path second.

## Constraints
- No changes to application source code.
- Artifacts in English except README / user-facing .bat messages, which follow the project's existing Spanish.
- Conventional commits, no AI attribution.
- TDD: not applicable (infra/docs only, no test runner for compose); functional check = `docker compose up` + HTTP smoke.

## Tasks
- [x] T1 — Docker infra: Dockerfiles, nginx conf, .dockerignore, docker-compose.yml, .env.example, .gitignore (route: delegated writer — 2+ non-trivial files)
- [x] T2 — INICIAR.bat / DETENER.bat (route: delegated writer, same unit)
- [x] T3 — README quick start (route: delegated writer, same unit)
- [x] T4 — Bring stack up with compose and verify: login page at :5173, `/api` reaches backend (route: inline)

## Acceptance criteria
- `docker compose up -d --build` from repo root starts all 3 services; frontend at http://localhost:5173 shows login; `/api/...` proxied to backend returns backend responses (401 without token).
- Fresh clone + `INICIAR.bat` works without editing files (example defaults usable).

## Progress / Evidence
_(updated per task)_
- T1-T3 written by delegated writer. `docker compose --env-file .env.example config`: OK.
- Frontend image uses `npx vite build` instead of `npm run build`: `tsc -b` fails on pre-existing error `src/services/columnaService.ts(49,5) TS2322` (out of scope, app code untouched).
- T4: `INICIAR.bat` started Docker Desktop itself, built both images, stack up. `planova-mysql` healthy, backend "Started PlanovaApplication", `GET :5173/` 200, `GET :5173/login` 200 (SPA fallback), `GET :5173/api/` 401 (proxied to backend). Login page rendered in browser.
- Fix found in T4: bare `timeout` can resolve to GNU coreutils when PATH has Git tools; bat now calls `%SystemRoot%\System32\timeout.exe`.

## Next step
Fix the `columnaService.ts` type error and switch frontend Dockerfile back to `npm run build`.
