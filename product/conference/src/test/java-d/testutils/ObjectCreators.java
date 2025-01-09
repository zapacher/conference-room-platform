package testutils;

import ee.ctob.api.Request;
import ee.ctob.data.Conference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ee.ctob.data.enums.Gender.MALE;

public class ObjectCreators {

    public static Request createRegitrationRequest(UUID coonferenceUUID) {
        return new Request(
                null,
                null,
                "Chuck",
                "Norris",
                MALE,
                "chuck.norris@hot.me",
                LocalDateTime.parse("1940-04-10T00:00:00"),
                coonferenceUUID,
                null,
                null
        );
    }

    public static Request createRegitrationCancelRequest(UUID validationUUID) {
        return new Request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                validationUUID,
                null
        );
    }

    public static Request createFeedbackRequest(UUID validationUUID, String feedbackText) {
        return new Request(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                validationUUID,
                feedbackText
        );
    }

    public static Request createRequestForConfernces(String from, String until) {
        return new Request(
                LocalDateTime.parse(from),
                LocalDateTime.parse(until),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static Optional<List<Conference>> getConferenceList() {
        Conference conference1 = Conference.builder()
                .conferenceUUID(UUID.randomUUID())
                .roomUUID(UUID.randomUUID())
                .participants(List.of(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID()))
                .info("Some info conference1")
                .bookedFrom(LocalDateTime.parse("2024-12-10T16:00:00"))
                .bookedUntil(LocalDateTime.parse("2024-12-10T20:00:00"))
                .build();
        Conference conference2 = Conference.builder()
                .conferenceUUID(UUID.randomUUID())
                .roomUUID(UUID.randomUUID())
                .participants(List.of(UUID.randomUUID(),UUID.randomUUID()))
                .info("Some info conference2")
                .bookedFrom(LocalDateTime.parse("2024-12-11T10:00:00"))
                .bookedUntil(LocalDateTime.parse("2024-12-11T12:00:00"))
                .build();
        Conference conference3 = Conference.builder()
                .conferenceUUID(UUID.randomUUID())
                .roomUUID(UUID.randomUUID())
                .participants(List.of(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),
                        UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID()))
                .info("Some info conference3")
                .bookedFrom(LocalDateTime.parse("2024-12-18T16:00:00"))
                .bookedUntil(LocalDateTime.parse("2024-12-18T16:20:00"))
                .build();
        Conference conference4 = Conference.builder()
                .conferenceUUID(UUID.randomUUID())
                .roomUUID(UUID.randomUUID())
                .participants(List.of(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID()))
                .info("Some info conference4")
                .bookedFrom(LocalDateTime.parse("2024-12-23T20:00:00"))
                .bookedUntil(LocalDateTime.parse("2024-12-24T20:00:00"))
                .build();
        Conference conference5 = Conference.builder()
                .conferenceUUID(UUID.randomUUID())
                .roomUUID(UUID.randomUUID())
                .participants(new ArrayList<>())
                .info("Some info conference5")
                .bookedFrom(LocalDateTime.parse("2024-12-30T18:00:00"))
                .bookedUntil(LocalDateTime.parse("2024-12-30T22:00:00"))
                .build();
        return Optional.of(List.of(conference1, conference2, conference3, conference4, conference5));
    }
}
