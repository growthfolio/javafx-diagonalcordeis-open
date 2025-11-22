-- Configurar SQLite para WAL mode e otimizações
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;
PRAGMA cache_size = 10000;
PRAGMA temp_store = memory;
PRAGMA mmap_size = 268435456; -- 256MB
PRAGMA busy_timeout = 30000;