package ee.ctob.services;

import ee.ctob.api.Feedback;
import ee.ctob.api.dto.ConferenceDTO;
import ee.ctob.data.Conference;
import ee.ctob.data.Participant;
import ee.ctob.data.access.ConferenceDAO;
import ee.ctob.data.access.ParticipantDAO;
import ee.ctob.data.access.RoomDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ee.ctob.data.enums.ConferenceStatus.AVAILABLE;
import static java.time.LocalDateTime.now;

@Service
public class ConferenceService {
    @Autowired
    ConferenceDAO conferenceDAO;
    @Autowired
    RoomDAO roomDAO;
    @Autowired
    ParticipantDAO participantDAO;

    public ConferenceDTO create(ConferenceDTO conferenceDTO) {
        if(now().isAfter(conferenceDTO.getBookedFrom())) {
            return ConferenceDTO.builder()
                    .info("Conference start time must be in future")
                    .build();
        }

        if(roomDAO.isRoomAvailable(conferenceDTO.getRoomUUID())==0) {
            return ConferenceDTO.builder()
                    .info("Chosen room isn't available")
                    .build();
        }

        if(conferenceDAO.countOverlappingBookingsByRoomUUID(conferenceDTO.getRoomUUID(), conferenceDTO.getBookedFrom(), conferenceDTO.getBookedUntil())>0) {
            return ConferenceDTO.builder()
                    .info("Chosen time isn't available")
                    .build();
        }

        Conference conference = conferenceDAO.saveAndFlush(Conference.builder()
                .roomUUID(conferenceDTO.getRoomUUID())
                .conferenceUUID(UUID.randomUUID())
                .validationUUID(UUID.randomUUID())
                .status(AVAILABLE)
                .info(conferenceDTO.getInfo())
                .bookedFrom(conferenceDTO.getBookedFrom())
                .bookedUntil(conferenceDTO.getBookedUntil())
                .build());

        return ConferenceDTO.builder()
                .conferenceUUID(conference.getConferenceUUID())
                .validationUUID(conference.getValidationUUID())
                .bookedFrom(conference.getBookedFrom())
                .bookedUntil(conference.getBookedUntil())
                .build();

    }

    public ConferenceDTO update(ConferenceDTO conferenceDTO) {
        if(conferenceDTO.getRoomUUID() != null) {
            if(conferenceDAO.countOverlappingBookingsByRoomUUID(conferenceDTO.getRoomUUID(), conferenceDTO.getBookedFrom(), conferenceDTO.getBookedUntil())==0) {
                if(conferenceDAO.cancelConference(conferenceDTO.getValidationUUID())==0){
                    return ConferenceDTO.builder()
                            .info("Conference doesn't exists")
                            .build();
                }

                Conference conference = conferenceDAO.saveAndFlush(Conference.builder()
                        .roomUUID(conferenceDTO.getRoomUUID())
                        .conferenceUUID(UUID.randomUUID())
                        .validationUUID(UUID.randomUUID())
                        .status(AVAILABLE)
                        .info(conferenceDTO.getInfo())
                        .bookedFrom(conferenceDTO.getBookedFrom())
                        .bookedUntil(conferenceDTO.getBookedUntil())
                        .build());

                return ConferenceDTO.builder()
                        .conferenceUUID(conference.getConferenceUUID())
                        .validationUUID(conference.getValidationUUID())
                        .bookedFrom(conference.getBookedFrom())
                        .bookedUntil(conference.getBookedUntil())
                        .oldValidationUUID(conferenceDTO.getValidationUUID())
                        .build();
            }
        }

        if(conferenceDAO.countOverlappingBookingsForUpdate(conferenceDTO.getValidationUUID(), conferenceDTO.getBookedFrom(), conferenceDTO.getBookedUntil())==0) {
            Conference conference = conferenceDAO.updateConference(conferenceDTO.getValidationUUID(), conferenceDTO.getBookedFrom(), conferenceDTO.getBookedUntil(), UUID.randomUUID());
            if (conference == null) {
                return ConferenceDTO.builder()
                        .info("Conference doesn't exists")
                        .build();
            }
            return ConferenceDTO.builder()
                    .conferenceUUID(conference.getConferenceUUID())
                    .validationUUID(conference.getValidationUUID())
                    .bookedFrom(conference.getBookedFrom())
                    .bookedUntil(conference.getBookedUntil())
                    .oldValidationUUID(conferenceDTO.getValidationUUID())
                    .build();
        }
        return ConferenceDTO.builder()
                .info("New values are conflicting with existing conferences")
                .build();
    }

    public ConferenceDTO checkFreeSpace(ConferenceDTO conferenceDTO) {
        Conference conference = conferenceDAO.getConferenceByValidationUUID(conferenceDTO.getValidationUUID());

        if(conference == null) {
            return ConferenceDTO.builder()
                    .validationUUID(conferenceDTO.getValidationUUID())
                    .info("Conference isn't available")
                    .build();
        }

        Integer roomCapacity = roomDAO.getRoomCapacityByRoomId(conference.getRoomUUID());
        Integer participantsCount = 0;
        if(!conference.getParticipants().isEmpty()) {
            participantsCount = conference.getParticipants().size();
        }

        return ConferenceDTO.builder()
                .validationUUID(conferenceDTO.getValidationUUID())
                .availableSpace(roomCapacity - participantsCount)
                .roomCapacity(roomCapacity)
                .participantsCount(participantsCount)
                .build();
    }

    public ConferenceDTO feedbackList(ConferenceDTO conferenceDTO) {
        Conference conference = conferenceDAO.getConferenceByValidationUUID(conferenceDTO.getValidationUUID());
        if(conference == null) {
            return  ConferenceDTO.builder()
                    .validationUUID(conferenceDTO.getValidationUUID())
                    .info("Conference doesn't exists")
                    .build();
        }

        List<UUID> participantUUIDList = conference.getParticipants();
        if(participantUUIDList.isEmpty()) {
            return  ConferenceDTO.builder()
                    .validationUUID(conferenceDTO.getValidationUUID())
                    .info("Conference has no participants")
                    .build();
        }

        List<Participant> participantList = participantDAO.findByParticipantUUIDs(participantUUIDList);
        if(participantList.isEmpty()) {
            return  ConferenceDTO.builder()
                    .validationUUID(conferenceDTO.getValidationUUID())
                    .info("No feedback for this conference")
                    .build();
        }

        List<Feedback> feedbackList = new ArrayList<>();
        for(Participant participant : participantList) {
            String shortName = "";
            shortName = shortName + Character.toUpperCase(participant.getFirstName().charAt(0))+".";
            shortName = shortName + Character.toUpperCase(participant.getLastName().charAt(0))+".";

            feedbackList.add(Feedback.builder()
                    .shortName(shortName)
                    .feedback(participant.getFeedback())
                    .build()
            );
        }

        return ConferenceDTO.builder()
                .validationUUID(conferenceDTO.getValidationUUID())
                .feedbackList(feedbackList)
                .build();
    }

    public ConferenceDTO cancel(ConferenceDTO conferenceDTO) {
        if (conferenceDAO.cancelConference(conferenceDTO.getValidationUUID())>0) {
            return ConferenceDTO.builder()
                    .oldValidationUUID(conferenceDTO.getValidationUUID())
                    .build();
        }

        return ConferenceDTO.builder()
                .info("Conference is already canceled or not exists")
                .build();
    }
}
