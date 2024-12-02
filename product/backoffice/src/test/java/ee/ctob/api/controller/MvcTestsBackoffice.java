package ee.ctob.api.controller;

import ee.ctob.api.Request;
import ee.ctob.api.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import testutils.TestContainer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static testutils.ObjectCreators.createRoomCreateRequest;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
public class MvcTestsBackoffice extends TestContainer {

    @Autowired
    MockMvc mockMvc;

    private Integer roomCapacity = 100;
    private Response response;
    private Request request;

    @Test
    void roomCreate() throws Exception {
        request = createRoomCreateRequest();
        performMock("/backoffice/room/create");
        assertAll( "Room create success",
                ()-> assertNotNull("Response", response),
                ()-> assertNotNull("roomUUID", response.getRoomUUID()),
                ()-> assertNotNull("validationUUID", response.getValidationUUID()),
                ()-> assertNull("reason", response.getReason())
        );
    }



    private void performMock(String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String responseMvc = mockMvc.perform(post(path)
                        .contentType("application/json;charset=UTF-8")
                        .content(mapper.writeValueAsString(request)))
                .andExpect(content().contentType("application/json"))
                .andReturn().getResponse().getContentAsString();

        response = mapper.readValue(responseMvc, Response.class);
    }
}