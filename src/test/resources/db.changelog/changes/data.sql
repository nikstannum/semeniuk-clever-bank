INSERT INTO banks("name", bank_identifier)
VALUES ('Belarusbank', 'BLRB'),
	('Priorbank', 'PRBP'),
	('ALFA-bank', 'ALBP'),
	('Paritet-bank', 'PTBP'),
	('Clever-bank', 'CLBP'),
	('BNB-bank', 'BNBP');

INSERT INTO users(first_name, last_name, email)
VALUES ('Andrew', 'Ivanov', 'ivanov@mail.com'),
	('Ihar', 'Petrov', 'petrov@mail.com'),
	('Sergey', 'Alekseev', 'alekseev@mail.com'),
	('Alexey', 'Sergeev', 'sergeev@mail.com'),
	('Vladimir', 'Sidorov', 'sidorov@mail.com'),
	('Petr', 'Fedorov', 'fedorov@mail.com'),
	('Andrew', 'Pavlov', 'apavlov@mail.com'),
	('Pavel', 'Grigorev', 'grigorev@mail.com'),
	('Oksana', 'Andreeva', 'oandreeva@mail.com'),
	('Vsevolod', 'Borovikov', 'vborovikov@mail.com'),
	('Anastasia', 'Petrova', 'anstptrova@mail.com'),
	('Maksim', 'Zhirnov', 'zhirnov@mail.com'),
	('Michael', 'Galkin', 'galkin@mail.com'),
	('Vladislav', 'Tarasov', 'tarasov@mail.com'),
	('Maria', 'Ivanova', 'mrivanova@mail.com'),
	('Alesia', 'Timchenko', 'atimchenko@mail.com'),
	('Roman', 'Feoktistov', 'feoktistov@mail.com'),
	('Vasiliy', 'Smirnov', 'vsmirnov@mail.com'),
	('Veronika', 'Simkina', 'simkina@mail.com'),
	('Ilya', 'Smirnov', 'ismirnov@mail.com'),
	('Konstantin', 'Novikov', 'knovikov@mail.com'),
	('Valeryi', 'Zadornov', 'vzadornov@mail.com');

INSERT INTO currencies ("name")
VALUES ('USD'),
('BYN'),
('EUR'),
('GDP');

INSERT INTO accounts(user_id, bank_id, amount, currency_id)
VALUES
((SELECT id FROM users WHERE email = 'ivanov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 100.11, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'petrov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 111.00, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'alekseev@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 19.19, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'sergeev@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PRBP'), 1541.18, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'sidorov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'ALBP'), 154.18, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'fedorov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PTBP'), 914.00, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'apavlov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BNBP'), 8252.52, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'grigorev@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 456.65, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'oandreeva@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 87.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'vborovikov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PRBP'), 985.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'anstptrova@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'ALBP'), 98.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'zhirnov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PTBP'), 918.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'galkin@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BNBP'), 918.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'tarasov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 918.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'mrivanova@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 918.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'atimchenko@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PRBP'), 918.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'feoktistov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'ALBP'), 918.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'vsmirnov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PTBP'), 918.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'simkina@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BNBP'), 918.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'ismirnov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 918.17, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'knovikov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 918.17, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'vzadornov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PRBP'), 918.17, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'ivanov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BNBP'), 100.11, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'petrov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 10.00, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'alekseev@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 19.19, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'sergeev@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 1541.18, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'sidorov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PTBP'), 154.18, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'fedorov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'ALBP'), 914.00, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'apavlov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 8252.52, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'grigorev@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BNBP'), 456.65, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'oandreeva@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'ALBP'), 87.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'vborovikov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 985.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'anstptrova@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PTBP'), 98.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'zhirnov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'ALBP'), 918.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'galkin@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 918.17, (SELECT id FROM currencies WHERE name = 'EUR')),
((SELECT id FROM users WHERE email = 'tarasov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 918.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'mrivanova@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 918.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'atimchenko@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PRBP'), 918.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'feoktistov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'ALBP'), 918.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'vsmirnov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PTBP'), 918.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'simkina@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BNBP'), 918.17, (SELECT id FROM currencies WHERE name = 'USD')),
((SELECT id FROM users WHERE email = 'ismirnov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'BLRB'), 918.17, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'knovikov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 918.17, (SELECT id FROM currencies WHERE name = 'BYN')),
((SELECT id FROM users WHERE email = 'knovikov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'CLBP'), 0, (SELECT id FROM currencies WHERE name = 'GDP')),
((SELECT id FROM users WHERE email = 'vzadornov@mail.com'), (SELECT id FROM banks WHERE bank_identifier = 'PRBP'), 918.17, (SELECT id FROM currencies WHERE name = 'BYN'));


