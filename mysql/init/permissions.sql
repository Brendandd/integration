-- inside init/01-setup-user.sql (or your init.sql)
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON mydb.* TO 'admin'@'%';
FLUSH PRIVILEGES;