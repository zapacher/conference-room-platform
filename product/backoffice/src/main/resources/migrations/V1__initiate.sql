CREATE TABLE conferences (
    id SERIAL PRIMARY KEY,
    conference_uuid UUID UNIQUE NOT NULL,
    validation_uuid UUID UNIQUE NOT NULL,
    room_uuid UUID,
    status VARCHAR(255) NOT NULL,
    info TEXT,
    booked_from TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    booked_until TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE TABLE conference_participants (
    conference_id INT NOT NULL,
    participant_uuid UUID UNIQUE NOT NULL,
    PRIMARY KEY (conference_id, participant_uuid),
    FOREIGN KEY (conference_id) REFERENCES conferences(id) ON DELETE CASCADE
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

CREATE INDEX idx_conference_id ON conference_participants(conference_id);