//package ee.ctob.service;
//
//import ee.ctob.api.ConferenceAvailable;
//import ee.ctob.api.dto.ParticipantDTO;
//import ee.ctob.data.Conference;
//import ee.ctob.data.Participant;
//import ee.ctob.data.access.ConferenceDAO;
//import ee.ctob.data.access.ParticipantDAO;
//import ee.ctob.data.access.RoomDAO;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import static java.time.Instant.now;
//
//@Service
//public class ParticipantService {
//    @Autowired
//    ParticipantDAO participantDAO;
//    @Autowired
//    ConferenceDAO conferenceDAO;
//    @Autowired
//    RoomDAO roomDAO;
//
//    public UUID registration(ParticipantDTO participantDTO) {
//        Participant participant = participantDAO.saveAndFlush(
//                Participant.builder()
//                        .created(now())
//                        .validationUUID(UUID.randomUUID())
//                        .firstName(participantDTO.getFirstName())
//                        .lastName(participantDTO.getLastName())
//                        .email(participantDTO.getEmail())
//                        .gender(participantDTO.getGender())
//                        .dateOfBirth(participantDTO.getDateOfBirth())
//                        .build()
//        );
//        return participant.getValidationUUID();
//    }
//
//    public ParticipantDTO registrationCancel(ParticipantDTO participantDTO) {
//        boolean canceled = participantDAO.deleteByValidationUUID(participantDTO.getValidationUUID())>0;
//        return ParticipantDTO.builder()
//                .registrationCancel(canceled)
//                .build();
//    }
//
//    public ParticipantDTO feedback(ParticipantDTO participantDTO) {
//        boolean result = participantDAO.feedback(participantDTO.getValidationUUID(), participantDTO.getFeedback())>0;
//
//        ParticipantDTO.ParticipantDTOBuilder participantDTOBuilder = ParticipantDTO.builder()
//                .validationUUID(participantDTO.getValidationUUID())
//                .feedbackResult(result);
//
//        if(!result) {
//            return participantDTOBuilder.info("Feedback already exists").build();
//        }
//
//        return participantDTOBuilder.build();
//    }
//
//    public ParticipantDTO availableConferences(ParticipantDTO participantDTO) {
////        List<Conference> conferenceList = conferenceDAO.findAllAvailableBetween(participantDTO.getFrom(), participantDTO.getUntil());
////
////        List<ConferenceAvailable> conferenceAvailableList = new ArrayList<>();
////
////        for(Conference conference : conferenceList) {
////            conferenceAvailableList.add(ConferenceAvailable.builder()
////                            .conferenceUUID(conference.getConferenceUUID())
////                            .location(roomDAO.getRoomLocationByRoomId(conference.getRoomUUID()))
////                            .participantsAmount(conference.getParticipants().size())
////                    .build());
////        }
//        return null;
//    }
//}
