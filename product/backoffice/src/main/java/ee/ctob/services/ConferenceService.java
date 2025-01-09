package ee.ctob.services;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.ParticipantDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ConferenceDTO;
import ee.ctob.api.error.BadRequestException;
import ee.ctob.api.error.PreconditionsFailedException;
import ee.ctob.data.Conference;
import ee.ctob.data.Participant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ee.ctob.data.enums.ConferenceStatus.AVAILABLE;
import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
@Transactional
public class ConferenceService {

    final ConferenceDAO conferenceDAO;
    final RoomDAO roomDAO;
    final ParticipantDAO participantDAO;

    public ConferenceDTO create(ConferenceDTO conferenceDTO) {
        if(now().isAfter(conferenceDTO.getBookedFrom()) || conferenceDTO.getBookedFrom().isAfter(conferenceDTO.getBookedUntil())) {
            throw new BadRequestException(400, "Unlogical booking time");
        }

        roomDAO.isRoomAvailable(conferenceDTO.getRoomUUID())
                .orElseThrow(()-> new PreconditionsFailedException("Chosen room isn't available"));

        if(conferenceDAO.countOverlappingBookingsByRoomUUID(conferenceDTO.getRoomUUID(), conferenceDTO.getBookedFrom(), conferenceDTO.getBookedUntil())>0) {
            throw new PreconditionsFailedException("Chosen time isn't available");
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
        if(!timeGoodFormat(conferenceDTO)) {
            throw new BadRequestException(400, "Booking time is bad formatted");
        }

        if(conferenceDTO.getRoomUUID() != null) {
            if(conferenceDAO.countOverlappingBookingsByRoomUUID(conferenceDTO.getRoomUUID(), conferenceDTO.getBookedFrom(), conferenceDTO.getBookedUntil())==0) {
                if(conferenceDAO.cancelConference(conferenceDTO.getValidationUUID())==0) {
                    throw new PreconditionsFailedException("Conference doesn't exists");
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
        }

        if(conferenceDAO.countOverlappingBookingsForUpdate(conferenceDTO.getValidationUUID(), conferenceDTO.getBookedFrom(), conferenceDTO.getBookedUntil())==0) {
            Conference conference = conferenceDAO.updateConference(conferenceDTO.getValidationUUID(), conferenceDTO.getBookedFrom(), conferenceDTO.getBookedUntil(), UUID.randomUUID())
                    .orElseThrow(()-> new PreconditionsFailedException("Conference doesn't exists"));
            return ConferenceDTO.builder()
                    .conferenceUUID(conference.getConferenceUUID())
                    .validationUUID(conference.getValidationUUID())
                    .bookedFrom(conference.getBookedFrom())
                    .bookedUntil(conference.getBookedUntil())
                    .build();
        }

        throw new PreconditionsFailedException("New values are conflicting with existing conferences");
    }

    public ConferenceDTO checkFreeSpace(ConferenceDTO conferenceDTO) {
        Conference conference = conferenceDAO.getConferenceByValidationUUID(conferenceDTO.getValidationUUID())
                .orElseThrow(()-> new PreconditionsFailedException("Conference isn't available"));

        Integer roomCapacity = roomDAO.getRoomCapacityByRoomId(conference.getRoomUUID());
        Integer participantsCount = conference.getParticipants().size();

        return ConferenceDTO.builder()
                .validationUUID(conferenceDTO.getValidationUUID())
                .availableSpace(roomCapacity - participantsCount)
                .roomCapacity(roomCapacity)
                .participantsCount(participantsCount)
                .build();
    }

    public ConferenceDTO feedbackList(ConferenceDTO conferenceDTO) {
        Conference conference = conferenceDAO.getConferenceByValidationUUID(conferenceDTO.getValidationUUID())
                .orElseThrow(() -> new PreconditionsFailedException("Conference doesn't exists"));

        List<UUID> participantUUIDList = conference.getParticipants();
        if(participantUUIDList.isEmpty()) {
            throw new PreconditionsFailedException("Conference has no participants");
        }

        List<Participant> participantList = participantDAO.findByParticipantUUIDs(participantUUIDList)
                .orElseThrow(()-> new PreconditionsFailedException("No feedback for this conference"));

        if(participantList.isEmpty()) {
            throw new PreconditionsFailedException("No feedback for this conference");
        }

        List<Response.Feedback> feedbackList = new ArrayList<>();
        for(Participant participant : participantList) {
            String shortName = "";
            shortName = shortName + Character.toUpperCase(participant.getFirstName().charAt(0))+".";
            shortName = shortName + Character.toUpperCase(participant.getLastName().charAt(0))+".";

            feedbackList.add(Response.Feedback.builder()
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
                    .validationUUID(conferenceDTO.getValidationUUID())
                    .build();
        }

        throw new PreconditionsFailedException("Conference is already canceled or not exists");
    }

    private boolean timeGoodFormat(ConferenceDTO conferenceDTO) {
        LocalDateTime from = conferenceDTO.getBookedFrom();
        LocalDateTime until = conferenceDTO.getBookedUntil();
        LocalDateTime current =  now();
        return !current.isAfter(from) && !from.isAfter(until);
    }
}
