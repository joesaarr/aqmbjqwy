CREATE TABLE IF NOT EXISTS account (
    id serial,
    customer_id varchar(255) NOT NULL,
    country varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS balance (
    id serial,
    amount numeric NOT NULL  CHECK (amount >= 0),
    currency varchar(255) NOT NULL,
    account_id int NOT NULL,
    CONSTRAINT fk_account
        FOREIGN KEY(account_id)
            REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS transaction (
    id serial,
    account_id int NOT NULL,
    amount numeric NOT NULL,
    currency varchar(255) NOT NULL,
    direction varchar(255) NOT NULL,
    description varchar(255),
    balance_after real NOT NULL,
    CONSTRAINT fk_account
        FOREIGN KEY(account_id)
            REFERENCES account(id)
);

