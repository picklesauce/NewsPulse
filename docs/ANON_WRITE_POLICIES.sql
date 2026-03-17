-- Required RLS policies for app-managed (anon-key) persistence.
-- Run this in Supabase SQL editor.

-- user_profiles
alter table public.user_profiles enable row level security;
drop policy if exists "anon can insert user_profiles" on public.user_profiles;
create policy "anon can insert user_profiles"
on public.user_profiles
for insert
to anon
with check (true);

drop policy if exists "anon can update user_profiles" on public.user_profiles;
create policy "anon can update user_profiles"
on public.user_profiles
for update
to anon
using (true)
with check (true);

drop policy if exists "anon can read user_profiles" on public.user_profiles;
create policy "anon can read user_profiles"
on public.user_profiles
for select
to anon
using (true);

-- followed_interests
alter table public.followed_interests enable row level security;
drop policy if exists "anon can insert followed_interests" on public.followed_interests;
create policy "anon can insert followed_interests"
on public.followed_interests
for insert
to anon
with check (true);

drop policy if exists "anon can delete followed_interests" on public.followed_interests;
create policy "anon can delete followed_interests"
on public.followed_interests
for delete
to anon
using (true);

drop policy if exists "anon can read followed_interests" on public.followed_interests;
create policy "anon can read followed_interests"
on public.followed_interests
for select
to anon
using (true);

-- saved_articles
alter table public.saved_articles enable row level security;
drop policy if exists "anon can insert saved_articles" on public.saved_articles;
create policy "anon can insert saved_articles"
on public.saved_articles
for insert
to anon
with check (true);

drop policy if exists "anon can delete saved_articles" on public.saved_articles;
create policy "anon can delete saved_articles"
on public.saved_articles
for delete
to anon
using (true);

drop policy if exists "anon can read saved_articles" on public.saved_articles;
create policy "anon can read saved_articles"
on public.saved_articles
for select
to anon
using (true);

-- reading_history
alter table public.reading_history enable row level security;
drop policy if exists "anon can insert reading_history" on public.reading_history;
create policy "anon can insert reading_history"
on public.reading_history
for insert
to anon
with check (true);

drop policy if exists "anon can read reading_history" on public.reading_history;
create policy "anon can read reading_history"
on public.reading_history
for select
to anon
using (true);

-- articles (needed when app upserts unseen articles before save/history)
alter table public.articles enable row level security;
drop policy if exists "anon can insert articles" on public.articles;
create policy "anon can insert articles"
on public.articles
for insert
to anon
with check (true);

drop policy if exists "anon can update articles" on public.articles;
create policy "anon can update articles"
on public.articles
for update
to anon
using (true)
with check (true);
