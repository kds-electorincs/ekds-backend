## Setup

1. Install PostgreSQL 17 from https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
    - Remember the postgres superuser password you set during install
    - Add `C:\Program Files\PostgreSQL\17\bin` to your PATH
2. Install Java 25 (Gradle will auto-download it if missing)
3. Clone this repo
4. Run `setup.bat` (double-click or from terminal) — creates database and `.env`
5. Run `./gradlew :kds-web:bootRun`