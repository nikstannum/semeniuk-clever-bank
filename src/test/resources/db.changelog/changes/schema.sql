--CREATE DATABASE bank;
--DROP DATABASE bank;

DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS currencies;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS banks;

CREATE TABLE IF NOT EXISTS banks(
	id BIGSERIAL PRIMARY KEY,
	"name" VARCHAR (50) NOT NULL,
	bank_identifier char(4) NOT NULL,
	deleted boolean DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS users (
	id BIGSERIAL PRIMARY KEY,
	first_name VARCHAR (30),
	last_name VARCHAR (30),
	email VARCHAR (50) NOT NULL,
	deleted boolean DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS email_index ON users (email);

CREATE TABLE IF NOT EXISTS currencies (
	id SERIAL PRIMARY KEY,
	"name" VARCHAR (10) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
	id BIGSERIAL PRIMARY KEY,
	"number" char(28) UNIQUE NOT NULL,
	user_id BIGINT NOT NULL REFERENCES users,
	bank_id BIGINT NOT NULL REFERENCES banks,
	amount NUMERIC(20,2),
	currency_id INTEGER NOT NULL REFERENCES currencies,
	open_time DATE NOT NULL DEFAULT current_date,
	deleted boolean DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS number_index ON accounts ("number");

CREATE TABLE IF NOT EXISTS transactions (
id BIGSERIAL PRIMARY KEY,
account_id BIGINT,
destination_account_id BIGINT,
account_amount NUMERIC(20,2),
destination_account_amount NUMERIC(20,2),
"time" TIMESTAMP (0) WITHOUT time ZONE DEFAULT (now() at time zone 'utc'),
deleted boolean DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS account_id_index ON transactions(account_id);
CREATE INDEX IF NOT EXISTS destination_account_id_index ON transactions(destination_account_id);

create or replace function trigger_insert()
	RETURNS trigger AS $$
	 declare identifier varchar(4);
	 account_id varchar(24);
begin
	if NEW.number IS null
	THEN
	select bank_identifier into identifier from banks where id = NEW.bank_id;
	account_id := right(cast(NEW.id as varchar), 24);
	account_id :=  lpad(account_id, 24, '0');
	NEW.number := identifier || account_id;
	end IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

create or replace trigger account_number_generate
	before insert
	on accounts
	for each row
	EXECUTE procedure trigger_insert();


create or replace function now_utc()
	RETURNS TIMESTAMP (0)
		RETURN now() at time zone 'utc';

create or replace function trigger_update()
	RETURNS trigger AS $$
begin
	if NEW.account_id is DISTINCT FROM OLD.account_id
	or NEW.destination_account_id is DISTINCT FROM OLD.destination_account_id
	or NEW.account_amount is DISTINCT FROM OLD.account_amount
	or NEW.destination_account_amount is DISTINCT FROM OLD.destination_account_amount
	then
	update transactions
	set "time" = now_utc()
	where id = OLD.id;
	end if;
	return new;
end;
$$ LANGUAGE 'plpgsql';

create or replace trigger update_transaction_time
	after update
	on transactions
	for each row
	EXECUTE procedure trigger_update();

