CREATE TABLE participants (
    id SERIAL PRIMARY KEY,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    participant_uuid UUID UNIQUE NOT NULL,
    validation_uuid UUID UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    gender VARCHAR(255) NOT NULL,
    date_of_birth DATE WITHOUT TIME ZONE NOT NULL,
    feedback TEXT
);