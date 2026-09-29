-- Run through Supabase CLI / migration pipeline. Never expose this database to the browser.
create extension if not exists pgcrypto;

create table public.visitors (
  id uuid primary key,
  first_seen_at timestamptz not null default now(),
  last_seen_at timestamptz not null default now(),
  first_landing_path text,
  first_utm jsonb not null default '{}'::jsonb
);

create table public.landing_sessions (
  id uuid primary key,
  visitor_id uuid not null references public.visitors(id) on delete cascade,
  started_at timestamptz not null default now(),
  last_seen_at timestamptz not null default now(),
  landing_path text not null,
  referrer text,
  utm jsonb not null default '{}'::jsonb
);
create index landing_sessions_visitor_id_idx on public.landing_sessions(visitor_id);
create index landing_sessions_started_at_idx on public.landing_sessions(started_at);

create table public.tracking_events (
  id uuid primary key default gen_random_uuid(),
  visitor_id uuid not null references public.visitors(id) on delete cascade,
  session_id uuid not null references public.landing_sessions(id) on delete cascade,
  event_name text not null check (event_name in ('page_view', 'reserve_opened', 'reservation_completed', 'section_view', 'cta_clicked')),
  path text not null,
  occurred_at timestamptz not null default now(),
  properties jsonb not null default '{}'::jsonb
);
create unique index tracking_events_one_page_view_per_session_path_idx
  on public.tracking_events(visitor_id, session_id, path)
  where event_name = 'page_view';
create index tracking_events_occurred_at_idx on public.tracking_events(occurred_at);
create unique index tracking_events_one_section_view_per_session_section_idx
  on public.tracking_events(visitor_id, session_id, (properties->>'sectionId'))
  where event_name = 'section_view';

create table public.reservations (
  id uuid primary key default gen_random_uuid(),
  visitor_id uuid not null references public.visitors(id),
  session_id uuid not null references public.landing_sessions(id),
  reservation_type text not null check (reservation_type in ('QUICK_AI_INTERVIEW', 'REVERSE_MATCHING')),
  name text not null check (char_length(trim(name)) between 1 and 100),
  email text not null,
  normalized_email text generated always as (lower(trim(email))) stored,
  privacy_consent_at timestamptz not null,
  created_at timestamptz not null default now(),
  utm jsonb not null default '{}'::jsonb,
  unique (normalized_email)
);
create index reservations_created_at_idx on public.reservations(created_at);
create index reservations_type_idx on public.reservations(reservation_type);

-- Browser roles receive no table privileges. The Spring API uses a server-side DB role.
revoke all on public.visitors, public.landing_sessions, public.tracking_events, public.reservations from anon, authenticated;

-- Basic daily funnel query for the admin endpoint.
create view public.daily_landing_funnel as
select
  date_trunc('day', v.first_seen_at)::date as day,
  count(distinct v.id) as unique_visitors,
  count(distinct s.id) as sessions,
  count(distinct r.visitor_id) as reserving_visitors,
  count(r.id) as reservations,
  round(100.0 * count(distinct r.visitor_id) / nullif(count(distinct v.id), 0), 2) as visitor_conversion_rate
from public.visitors v
left join public.landing_sessions s on s.visitor_id = v.id
left join public.reservations r on r.visitor_id = v.id
group by 1;
