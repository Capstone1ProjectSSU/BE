-- ====================================
-- 1. 초기화 (FK 무시)
-- ====================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE sheet;
TRUNCATE TABLE audio;
TRUNCATE TABLE user;
TRUNCATE TABLE transcription_job;

SET FOREIGN_KEY_CHECKS = 1;


-- ====================================
-- 2. User 테이블
-- ====================================

INSERT INTO user (id, username, email, password, created_at, updated_at)
VALUES 
(1, 'testuser1', 'test1@example.com', 'password123', NOW(), NOW()),
(2, 'testuser2', 'test2@example.com', 'password123', NOW(), NOW()),
(3, 'testuser3', 'test3@example.com', 'password123', NOW(), NOW());


-- ====================================
-- 3. Audio 테이블
-- ====================================

INSERT INTO audio (id, user_id, title, artist, file_path, file_size, uploaded_at, created_at, updated_at)
VALUES 
(1, 1, 'Let It Be - Audio', 'The Beatles', '/uploads/audio/audio-let-it-be.mp3', 5242880, NOW(), NOW(), NOW()),
(2, 1, 'Wonderwall - Audio', 'Oasis', '/uploads/audio/audio-wonderwall.mp3', 6144000, NOW(), NOW(), NOW()),
(3, 1, 'Stairway to Heaven - Audio', 'Led Zeppelin', '/uploads/audio/audio-stairway.mp3', 8388608, NOW(), NOW(), NOW()),
(4, 1, 'Yesterday - Audio', 'The Beatles', '/uploads/audio/audio-yesterday.mp3', 3145728, NOW(), NOW(), NOW()),
(5, 1, 'Hotel California - Audio', 'Eagles', '/uploads/audio/audio-hotel.mp3', 7340032, NOW(), NOW(), NOW()),
(6, 2, 'Bohemian Rhapsody - Audio', 'Queen', '/uploads/audio/audio-bohemian.mp3', 9437184, NOW(), NOW(), NOW()),
(7, 2, 'Sweet Child O Mine - Audio', 'Guns N Roses', '/uploads/audio/audio-sweet-child.mp3', 6291456, NOW(), NOW(), NOW()),
(8, 3, 'Smoke on the Water - Audio', 'Deep Purple', '/uploads/audio/audio-smoke.mp3', 4718592, NOW(), NOW(), NOW());


-- ====================================
-- 4. Sheet 테이블
-- ====================================

INSERT INTO sheet 
(id, user_id, audio_id, title, artist, instrument, difficulty, tuning, capo, duration, tempo, song_key, sheet_data_url, thumbnail_url, created_at, updated_at)
VALUES
(1, 1, 1, 'Let It Be', 'The Beatles', 'GUITAR', 'EASY', 'STANDARD', 0, 243, 76, 'C', '/files/sheets/music-1.json', '/files/thumbnails/music-1.png', '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
(2, 1, 2, 'Wonderwall', 'Oasis', 'GUITAR', 'NORMAL', 'STANDARD', 2, 258, 87, 'Em', '/files/sheets/music-2.json', '/files/thumbnails/music-2.png', '2025-01-05 11:00:00', '2025-01-05 11:00:00'),
(3, 1, 3, 'Stairway to Heaven', 'Led Zeppelin', 'GUITAR', 'HARD', 'STANDARD', 0, 482, 72, 'Am', '/files/sheets/music-3.json', '/files/thumbnails/music-3.png', '2025-01-10 12:00:00', '2025-01-10 12:00:00'),
(4, 1, 4, 'Yesterday', 'The Beatles', 'GUITAR', 'EASY', 'STANDARD', 1, 125, 90, 'F', '/files/sheets/music-4.json', '/files/thumbnails/music-4.png', '2025-01-15 13:00:00', '2025-01-15 13:00:00'),
(5, 1, 5, 'Hotel California', 'Eagles', 'GUITAR', 'NORMAL', 'STANDARD', 0, 391, 74, 'Bm', '/files/sheets/music-5.json', '/files/thumbnails/music-5.png', '2025-01-20 14:00:00', '2025-01-20 14:00:00'),
(6, 1, 1, 'Let It Be (Bass)', 'The Beatles', 'BASS', 'EASY', 'STANDARD', 0, 243, 76, 'C', '/files/sheets/music-6.json', '/files/thumbnails/music-6.png', '2025-02-01 15:00:00', '2025-02-01 15:00:00'),
(7, 1, 2, 'Wonderwall (Easy Ver)', 'Oasis', 'GUITAR', 'EASY', 'STANDARD', 2, 258, 87, 'Em', '/files/sheets/music-7.json', '/files/thumbnails/music-7.png', '2025-02-05 16:00:00', '2025-02-05 16:00:00'),
(8, 1, 3, 'Stairway to Heaven (Intro)', 'Led Zeppelin', 'GUITAR', 'NORMAL', 'STANDARD', 0, 120, 72, 'Am', '/files/sheets/music-8.json', '/files/thumbnails/music-8.png', '2025-02-10 17:00:00', '2025-02-10 17:00:00'),

(9, 2, 6, 'Bohemian Rhapsody', 'Queen', 'GUITAR', 'HARD', 'STANDARD', 0, 355, 72, 'Bb', '/files/sheets/music-9.json', '/files/thumbnails/music-9.png', '2025-02-15 10:00:00', '2025-02-15 10:00:00'),
(10, 2, 7, 'Sweet Child O Mine', 'Guns N Roses', 'GUITAR', 'NORMAL', 'DROP_D', 0, 356, 122, 'D', '/files/sheets/music-10.json', '/files/thumbnails/music-10.png', '2025-02-20 11:00:00', '2025-02-20 11:00:00'),
(11, 2, 6, 'Bohemian Rhapsody (Piano)', 'Queen', 'PIANO', 'HARD', 'STANDARD', 0, 355, 72, 'Bb', '/files/sheets/music-11.json', '/files/thumbnails/music-11.png', '2025-02-25 12:00:00', '2025-02-25 12:00:00'),

(12, 3, 8, 'Smoke on the Water', 'Deep Purple', 'GUITAR', 'EASY', 'STANDARD', 0, 322, 112, 'Gm', '/files/sheets/music-12.json', '/files/thumbnails/music-12.png', '2025-03-01 13:00:00', '2025-03-01 13:00:00');


-- ====================================
-- 5. TranscriptionJob 테이블 (필요 시 추가 가능)
-- ====================================

-- 예시:
-- INSERT INTO transcription_job
-- (id, user_id, audio_id, sheet_id, ai_job_id, progress_percent, progress_stage, queued_at, created_at, updated_at)
-- VALUES
-- (1, 1, 1, 1, 'JOB-001', 0, 'PENDING', NOW(), NOW(), NOW());
