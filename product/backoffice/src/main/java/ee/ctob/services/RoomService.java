package ee.ctob.services;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.dto.RoomDTO;
import ee.ctob.data.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static ee.ctob.data.enums.RoomStatus.AVAILABLE;

@Service
@RequiredArgsConstructor
public class RoomService {

    final RoomDAO roomDAO;
    final ConferenceDAO conferenceDAO;

    public RoomDTO create(RoomDTO roomDTO) {
        Room room = roomDAO.saveAndFlush(
                Room.builder()
                        .name(roomDTO.getName())
                        .status(AVAILABLE)
                        .capacity(roomDTO.getCapacity())
                        .location(roomDTO.getLocation())
                        .roomUUID(UUID.randomUUID())
                        .validationUuid(UUID.randomUUID())
                        .build()
        );
        return RoomDTO.builder()
                .validationUUID(room.getValidationUuid())
                .roomUUID(room.getRoomUUID())
                .build();
    }

    public RoomDTO update(RoomDTO roomDTO) {
        if(roomDTO.getStatus() != null & roomDTO.getCapacity() != null) {
            return RoomDTO.builder()
                    .description("Please provide new room status OR new capacity")
                    .validationUUID(roomDTO.getValidationUUID())
                    .build();
        }
        Room room = null;

        if(roomDTO.getStatus() != null) {
            Room roomIf = roomDAO.getRoomByValidationUUID(roomDTO.getValidationUUID());
            if(roomIf == null) {
                return RoomDTO.builder()
                        .description("Room not found, check validationUUID")
                        .validationUUID(roomDTO.getValidationUUID())
                        .build();
            }

            if(roomDTO.getStatus() == roomIf.getStatus()) {
                return RoomDTO.builder()
                        .description("Room status is already : " + roomDTO.getStatus())
                        .validationUUID(roomDTO.getValidationUUID())
                        .build();
            }
            conferenceDAO.closeConferencesByRoomUUID(roomIf.getRoomUUID());

            room = roomDAO.updateStatus(roomDTO.getValidationUUID(), roomDTO.getStatus().name());
        }

        if(roomDTO.getCapacity() != null) {
            Integer currentCapacity = roomDAO.getRoomCapacity(roomDTO.getValidationUUID());

            if(currentCapacity == null) {
                return RoomDTO.builder()
                        .description("Room not found, check validationUUID")
                        .validationUUID(roomDTO.getValidationUUID())
                        .build();
            }

            if(currentCapacity>roomDTO.getCapacity()) {
                conferenceDAO.closeConferenceOverlappingCountByRoomUUID(roomDTO.getValidationUUID(), roomDTO.getCapacity());

            }
            room = roomDAO.updateCapacity(roomDTO.getValidationUUID(), roomDTO.getCapacity());
            roomDTO.setStatus(room.getStatus());

        }

        if(room == null) {
            return RoomDTO.builder()
                    .description("Please provide new room status or new capacity")
                    .validationUUID(roomDTO.getValidationUUID())
                    .build();
        }

        return RoomDTO.builder()
                .name(room.getName())
                .roomUUID(room.getRoomUUID())
                .capacity(room.getCapacity())
                .status(roomDTO.getStatus())
                .validationUUID(room.getValidationUuid())
                .build();
    }
}
