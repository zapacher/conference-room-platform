package ee.ctob.api.mapper;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import ee.ctob.api.dto.ConferenceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConferenceMapper {
    ConferenceMapper INSTANCE = Mappers.getMapper(ConferenceMapper.class);

    @Mapping(source = "from", target = "bookedFrom")
    @Mapping(source = "until", target = "bookedUntil")
    ConferenceDTO toConferenceDTO(Request request);

    Response toResponse(ConferenceDTO conferenceDTO);
}