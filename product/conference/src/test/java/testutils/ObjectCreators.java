package testutils;

import ee.ctob.api.Request;

import java.time.LocalDateTime;
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
}
