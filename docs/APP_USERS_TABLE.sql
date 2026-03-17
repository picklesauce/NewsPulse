-- Simple app-managed auth table (NOT Supabase Auth).
-- Stores email/password for sprint-level persistence requirements.

create table if not exists public.app_users (
    id uuid primary key,
    email text not null unique,
    password text not null,
    created_at timestamp with time zone not null default now()
);

alter table public.app_users enable row level security;

drop policy if exists "anon can read app_users by email" on public.app_users;
create policy "anon can read app_users by email"
on public.app_users
for select
to anon
using (true);

drop policy if exists "anon can insert app_users" on public.app_users;
create policy "anon can insert app_users"
on public.app_users
for insert
to anon
with check (true);
