package ee.ctob.services;

import ee.ctob.access.ConferenceDAO;
import ee.ctob.access.RoomDAO;
import ee.ctob.api.dto.RoomDTO;
import ee.ctob.api.error.BadRequestException;
import ee.ctob.data.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static ee.ctob.data.enums.RoomStatus.AVAILABLE;

@Service
@Transactional
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
            throw new BadRequestException(400, "Please provide new room status OR new capacity");
        }
        Room roomFromDAO = roomDAO.getRoomByValidationUUID(roomDTO.getValidationUUID())
                .orElseThrow(()-> new BadRequestException(400, "Room not found, check validationUUID"));
        Room room = null;

        if(roomDTO.getStatus() != null) {

            if(roomDTO.getStatus() == roomFromDAO.getStatus()) {
                throw new BadRequestException(400, "Room status is already : " + roomDTO.getStatus());
            }

            conferenceDAO.closeConferencesByRoomUUID(roomFromDAO.getRoomUUID());
            room = roomDAO.updateStatus(roomDTO.getValidationUUID(), roomDTO.getStatus().name());

        }

        if(roomDTO.getCapacity() != null) {
            if(roomFromDAO.getCapacity()>roomDTO.getCapacity()) {
                conferenceDAO.closeConferenceOverlappingCountByRoomUUID(roomDTO.getValidationUUID(), roomDTO.getCapacity());
            }

            room = roomDAO.updateCapacity(roomDTO.getValidationUUID(), roomDTO.getCapacity());
            roomDTO.setStatus(room.getStatus());
        }

        if(room == null) {
            throw new BadRequestException(400, "Please provide new room status or new capacity");
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
