package ee.ctob.api.mapper;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ParticipantDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;


@Mapper
public interface ParticipantMapper {

    ParticipantMapper INSTANCE = Mappers.getMapper(ParticipantMapper.class);

    ParticipantDTO toParticipantDTO(Request request);


    @Mapping(source = "info", target = "reason")
    Response toResponse(ParticipantDTO participantDTO);

    void updateParticipantDTO(@MappingTarget ParticipantDTO dto, Request request);
}