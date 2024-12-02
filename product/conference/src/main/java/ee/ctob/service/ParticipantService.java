package ee.ctob.service;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ParticipantDTO;
import ee.ctob.data.Conference;
import ee.ctob.data.Participant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDateTime.now;


@Service
public class ParticipantService {
    @Autowired
    ParticipantDAO participantDAO;
    @Autowired
    ConferenceDAO conferenceDAO;
    @Autowired
    RoomDAO roomDAO;

    public ParticipantDTO registration(ParticipantDTO participantDTO) {
        UUID participantUUID = UUID.randomUUID();
        if(conferenceDAO.registerParticipant(participantUUID, participantDTO.getConferenceUUID())==0) {
            return ParticipantDTO.builder()
                    .info("Registration isn't available for this conference")
                    .build();
        }

        Participant participant = participantDAO.saveAndFlush(
                Participant.builder()
                        .created(now())
                        .participantUUID(participantUUID)
                        .validationUUID(UUID.randomUUID())
                        .firstName(participantDTO.getFirstName())
                        .lastName(participantDTO.getLastName())
                        .email(participantDTO.getEmail())
                        .gender(participantDTO.getGender())
                        .dateOfBirth(participantDTO.getDateOfBirth())
                        .build()
        );

        return ParticipantDTO.builder()
                .validationUUID(participant.getValidationUUID())
                .build();
    }

    public ParticipantDTO registrationCancel(ParticipantDTO participantDTO) {
        Participant participant = participantDAO.getParticipantUUID(participantDTO.getValidationUUID());
        if(participant == null) {
            return ParticipantDTO.builder()
                    .info("Participant with this validation doesn't exists")
                    .build();
        }

        UUID participantUUID = participant.getValidationUUID();

        if(conferenceDAO.isAvailableForCancel(participantUUID) == null) {
            return ParticipantDTO.builder()
                    .info("Conference already started or finished")
                    .build();
        }

        if(conferenceDAO.cancelRegistration(participantUUID)==0) {
            return ParticipantDTO.builder()
                    .info("Validation uuid isnt valid")
                    .build();

        }
        return ParticipantDTO.builder()
                .registrationCancel(true)
                .build();
    }

    public ParticipantDTO feedback(ParticipantDTO participantDTO) {
        boolean result = participantDAO.feedback(participantDTO.getValidationUUID(), participantDTO.getFeedback())>0;

        ParticipantDTO.ParticipantDTOBuilder participantDTOBuilder = ParticipantDTO.builder()
                .validationUUID(participantDTO.getValidationUUID())
                .feedbackResult(result);

        if(!result) {
            return participantDTOBuilder.info("Feedback already exists or conference isn't finished").build();
        }

        return participantDTOBuilder.build();
    }

    public ParticipantDTO availableConferences(ParticipantDTO participantDTO) {
        if(now().isAfter(participantDTO.getFrom()) || participantDTO.getUntil().isBefore(participantDTO.getFrom())) {
            return ParticipantDTO.builder()
                    .info("Requested time isn't logical")
                    .build();
        }

        List<Conference> conferenceList = conferenceDAO.findAllAvailableBetween(participantDTO.getFrom(), participantDTO.getUntil());
        if(conferenceList.isEmpty()) {
            return  ParticipantDTO.builder()
                    .info("No conferences is available at this time period")
                    .build();
        }

        List<Response.ConferenceAvailable> conferenceAvailableList = new ArrayList<>();
        for(Conference conference : conferenceList) {
            conferenceAvailableList.add(Response.ConferenceAvailable.builder()
                    .conferenceUUID(conference.getConferenceUUID())
                    .location(roomDAO.getRoomLocationByRoomId(conference.getRoomUUID()))
                    .participantsAmount(conference.getParticipants().size())
                    .info(conference.getInfo())
                    .from(conference.getBookedFrom())
                    .until(conference.getBookedUntil())
                    .build());
        }

        return ParticipantDTO.builder()
                .conferenceAvailableList(conferenceAvailableList)
                .build();
    }
}
