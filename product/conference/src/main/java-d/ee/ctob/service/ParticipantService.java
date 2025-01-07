package ee.ctob.service;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ParticipantDTO;
import ee.ctob.api.error.BadRequestException;
import ee.ctob.api.error.PreconditionsFailedException;
import ee.ctob.data.Conference;
import ee.ctob.data.Participant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDateTime.now;


@Service
@Transactional
@RequiredArgsConstructor
public class ParticipantService {

    final ParticipantDAO participantDAO;
    final ConferenceDAO conferenceDAO;
    final RoomDAO roomDAO;

    public ParticipantDTO registration(ParticipantDTO participantDTO) {
        UUID participantUUID = UUID.randomUUID();
        if(conferenceDAO.registerParticipant(participantUUID, participantDTO.getConferenceUUID())==0) {
            throw new PreconditionsFailedException("Registration isn't available for this conference");
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
        Participant participant = participantDAO.getParticipant(participantDTO.getValidationUUID())
                .orElseThrow(()-> new PreconditionsFailedException("Participant with this validation doesn't exists"));

        UUID participantUUID = participant.getParticipantUUID();

        conferenceDAO.isAvailableForCancel(participantUUID)
                .orElseThrow(()-> new PreconditionsFailedException("Conference already started or finished"));


        if(conferenceDAO.cancelRegistration(participantUUID)==0) {
            throw new PreconditionsFailedException("Validation uuid isn't valid");
        }

        return ParticipantDTO.builder()
                .registrationCancel(true)
                .build();
    }

    public ParticipantDTO feedback(ParticipantDTO participantDTO) {
        if(participantDAO.feedback(participantDTO.getValidationUUID(), participantDTO.getFeedback())==0) {
            throw new PreconditionsFailedException("Feedback already exists or conference isn't finished");
        }

        return  ParticipantDTO.builder()
                .validationUUID(participantDTO.getValidationUUID())
                .feedbackResult(true)
                .build();
    }

    public ParticipantDTO availableConferences(ParticipantDTO participantDTO) {
        if(now().isAfter(participantDTO.getFrom()) || participantDTO.getUntil().isBefore(participantDTO.getFrom())) {
            throw new BadRequestException(400, "Requested time isn't logical");
        }

        List<Conference> conferenceList = conferenceDAO.findAllAvailableBetween(participantDTO.getFrom(), participantDTO.getUntil())
                .orElseThrow(()-> new PreconditionsFailedException("No conferences is available at this time period"));

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
