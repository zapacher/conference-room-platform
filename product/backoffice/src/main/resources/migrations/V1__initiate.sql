CREATE TABLE conferences (
    id SERIAL PRIMARY KEY,
    conference_uuid UUID UNIQUE NOT NULL,
    validation_uuid UUID UNIQUE NOT NULL,
    room_uuid UUID,
    participants BYTEA,
    status VARCHAR(255) NOT NULL,
    info TEXT,
    booked_from TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    booked_until TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    room_uuid UUID UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    validation_uuid UUID UNIQUE NOT NULL,
    capacity INT,
    location VARCHAR(255)
);
