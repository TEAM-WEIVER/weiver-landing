-- Existing environments may already have the initial analytics migration applied.
-- Extend the event allowlist and make section exposure idempotent per session.
alter table public.tracking_events
  drop constraint if exists tracking_events_event_name_check;

alter table public.tracking_events
  add constraint tracking_events_event_name_check
  check (event_name in (
    'page_view',
    'reserve_opened',
    'reservation_completed',
    'section_view',
    'cta_clicked'
  ));

create unique index if not exists tracking_events_one_section_view_per_session_section_idx
  on public.tracking_events(visitor_id, session_id, (properties->>'sectionId'))
  where event_name = 'section_view';
