package ee.ctob.api.mapper;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.RoomDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoomMapper {
    RoomMapper INSTANCE = Mappers.getMapper(RoomMapper.class);

    RoomDTO toRoomDTO(Request request);

    @Mapping(source = "status", target = "roomStatus")
    @Mapping(source = "capacity", target = "roomCapacity")
    Response toResponse(RoomDTO roomDTO);
}