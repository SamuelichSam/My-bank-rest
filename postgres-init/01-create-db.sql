SELECT 'CREATE DATABASE bank_cards_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'bank_cards_db')\gexec