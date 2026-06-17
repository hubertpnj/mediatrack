-- ============================================================
-- Test seed data
-- ============================================================

-- Movies
INSERT INTO media (id, dtype, title, release_date) OVERRIDING SYSTEM VALUE VALUES
    (1,  'MOVIE',  'Inception',              '2010-07-16'),
    (2,  'MOVIE',  'The Dark Knight',        '2008-07-18'),
    (3,  'MOVIE',  'Interstellar',           '2014-11-07'),
    (4,  'MOVIE',  'Parasite',               '2019-05-30'),
    (5,  'MOVIE',  'The Godfather',          '1972-03-24');

INSERT INTO movie (id, duration_minutes) VALUES
    (1,  148),
    (2,  152),
    (3,  169),
    (4,  132),
    (5,  175);

-- TV Shows
INSERT INTO media (id, dtype, title, release_date) OVERRIDING SYSTEM VALUE VALUES
    (6,  'TV_SHOW', 'Breaking Bad',           '2008-01-20'),
    (7,  'TV_SHOW', 'The Wire',               '2002-06-02'),
    (8,  'TV_SHOW', 'Chernobyl',              '2019-05-06');

INSERT INTO tv_show (id) VALUES (6), (7), (8);

INSERT INTO season (show_id, season_number, title, release_date) VALUES
    (6, 1, NULL, '2008-01-20'),
    (6, 2, NULL, '2009-03-08'),
    (6, 3, NULL, '2010-03-21'),
    (6, 4, NULL, '2011-07-17'),
    (6, 5, NULL, '2012-07-15'),
    (7, 1, NULL, '2002-06-02'),
    (7, 2, NULL, '2003-06-01'),
    (8, 1, NULL, '2019-05-06');

INSERT INTO episode (season_id, episode_number, title, duration_minutes, release_date)
SELECT s.id, ep.episode_number, ep.title, ep.duration_minutes, ep.release_date
FROM season s
JOIN (VALUES
    (6, 1, 1, 'Pilot',                              58, DATE '2008-01-20'),
    (6, 1, 2, 'Cat''s in the Bag',                  48, DATE '2008-01-27'),
    (6, 1, 3, 'And the Bag''s in the River',         48, DATE '2008-02-10'),
    (6, 2, 1, 'Seven Thirty-Seven',                 47, DATE '2009-03-08'),
    (6, 5, 1, 'Blood Money',                        47, DATE '2013-08-11'),
    (6, 5, 16,'Felina',                              55, DATE '2013-09-29'),
    (8, 1, 1, 'Please Remain Calm',                 62, DATE '2019-05-06'),
    (8, 1, 2, 'The Open Word',                      62, DATE '2019-05-13'),
    (8, 1, 3, 'Open Wide, O Earth',                 62, DATE '2019-05-20'),
    (8, 1, 4, 'The Happiness of All Mankind',       62, DATE '2019-05-27'),
    (8, 1, 5, 'Vichnaya Pamyat',                    62, DATE '2019-06-03')
) AS ep(show_id, season_number, episode_number, title, duration_minutes, release_date)
  ON s.show_id = ep.show_id AND s.season_number = ep.season_number;

-- Books
INSERT INTO media (id, dtype, title, release_date) OVERRIDING SYSTEM VALUE VALUES
    (9,  'BOOK',   'Dune',                   '1965-08-01'),
    (10, 'BOOK',   'The Pragmatic Programmer','1999-10-20'),
    (11, 'BOOK',   'Sapiens',                '2011-01-01');

INSERT INTO book (id, isbn, page_count) VALUES
    (9,  '9780441013593', 412),
    (10, '9780201616224', 352),
    (11, '9780099590088', 498);

-- Albums
INSERT INTO media (id, dtype, title, release_date) OVERRIDING SYSTEM VALUE VALUES
    (12, 'ALBUM',  'Dark Side of the Moon',  '1973-03-01'),
    (13, 'ALBUM',  'Kind of Blue',           '1959-08-17'),
    (14, 'ALBUM',  'OK Computer',            '1997-05-21');

INSERT INTO album (id, album_type) VALUES
    (12, 'LP'),
    (13, 'LP'),
    (14, 'LP');

INSERT INTO track (album_id, track_number, title, duration_seconds) VALUES
    (12, 1,  'Speak to Me / Breathe',         236),
    (12, 2,  'On the Run',                    216),
    (12, 3,  'Time',                          421),
    (12, 4,  'The Great Gig in the Sky',      284),
    (12, 5,  'Money',                         382),
    (12, 6,  'Us and Them',                   462),
    (12, 7,  'Any Colour You Like',           205),
    (12, 8,  'Brain Damage',                  228),
    (12, 9,  'Eclipse',                       128),
    (13, 1,  'So What',                       564),
    (13, 2,  'Freddie Freeloader',            584),
    (13, 3,  'Blue in Green',                 337),
    (13, 4,  'All Blues',                     693),
    (13, 5,  'Flamenco Sketches',             567),
    (14, 1,  'Airbag',                        284),
    (14, 2,  'Paranoid Android',              383),
    (14, 3,  'Subterranean Homesick Alien',   274),
    (14, 4,  'Exit Music (For a Film)',        245),
    (14, 5,  'Let Down',                      237),
    (14, 6,  'Karma Police',                  264),
    (14, 7,  'Fitter Happier',                116),
    (14, 8,  'Electioneering',                231),
    (14, 9,  'Climbing Up the Walls',         244),
    (14, 10, 'No Surprises',                  228),
    (14, 11, 'Lucky',                         257),
    (14, 12, 'The Tourist',                   325);

-- Games
INSERT INTO media (id, dtype, title, release_date) OVERRIDING SYSTEM VALUE VALUES
    (15, 'GAME',   'The Witcher 3',           '2015-05-19'),
    (16, 'GAME',   'Red Dead Redemption 2',   '2018-10-26'),
    (17, 'GAME',   'Hollow Knight',           '2017-02-24');

INSERT INTO game (id) VALUES (15), (16), (17);

INSERT INTO game_platform (game_id, platform) VALUES
    (15, 'PC'), (15, 'PS4'), (15, 'XBOX_ONE'), (15, 'SWITCH'),
    (16, 'PC'), (16, 'PS4'), (16, 'XBOX_ONE'),
    (17, 'PC'), (17, 'PS4'), (17, 'XBOX_ONE'), (17, 'SWITCH');

-- Parties (persons)
INSERT INTO party (id, dtype) OVERRIDING SYSTEM VALUE VALUES
    (1, 'Person'),
    (2, 'Person'),
    (3, 'Person'),
    (4, 'Person'),
    (5, 'Person'),
    (6, 'Organization'),
    (7, 'Organization');

INSERT INTO person (id, first_name, last_name, birth_date) VALUES
    (1, 'Christopher', 'Nolan',       '1970-07-30'),
    (2, 'Bong',        'Joon-ho',     '1969-09-14'),
    (3, 'Francis Ford','Coppola',     '1939-04-07'),
    (4, 'Bryan',       'Cranston',    '1956-03-07'),
    (5, 'Frank',       'Herbert',     '1920-10-08');

INSERT INTO organization (id, name, founded_year) VALUES
    (6, 'Warner Bros.',     1923),
    (7, 'CD Projekt Red',   1994);

-- Contributions
INSERT INTO contribution (party_id, media_id, role, display_order) VALUES
    (1, 1,  'DIRECTOR', 1),
    (1, 2,  'DIRECTOR', 1),
    (1, 3,  'DIRECTOR', 1),
    (2, 4,  'DIRECTOR', 1),
    (3, 5,  'DIRECTOR', 1),
    (4, 6,  'ACTOR',    1),
    (5, 9,  'AUTHOR',   1),
    (7, 15, 'DEVELOPER',1),
    (7, 16, 'DEVELOPER',1);

-- Reset sequences so future inserts don't collide
SELECT setval('media_id_seq',  (SELECT MAX(id) FROM media));
SELECT setval('party_id_seq',  (SELECT MAX(id) FROM party));
