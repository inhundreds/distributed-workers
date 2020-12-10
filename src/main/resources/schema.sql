DROP TABLE IF EXISTS JOB;
DROP TABLE IF EXISTS STATUS;

CREATE TABLE STATUS(
    description VARCHAR PRIMARY KEY
);

CREATE TABLE JOB (
    id INT PRIMARY KEY,
    url VARCHAR,
    status VARCHAR,
    http_status_code INT,
    foreign key (status) references STATUS(description)
);

