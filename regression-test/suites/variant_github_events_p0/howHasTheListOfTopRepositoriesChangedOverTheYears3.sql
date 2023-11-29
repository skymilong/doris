SELECT cast(v:repo.name as string), count() AS stars FROM github_events WHERE cast(v:type as string) = 'WatchEvent' AND year(cast(v:created_at as datetime)) = '2015' GROUP BY cast(v:repo.name as string) ORDER BY stars, cast(v:repo.name as string) DESC LIMIT 50
