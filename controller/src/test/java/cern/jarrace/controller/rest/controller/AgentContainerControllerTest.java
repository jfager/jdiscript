package cern.jarrace.controller.rest.controller;

import cern.jarrace.controller.domain.Entrypoint;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.HttpMethod;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.NestedServletException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Created by timartin on 25/01/2016.
 */

public class AgentContainerControllerTest {

    private MockMvc mockMvc;
    private AgentContainerController agentContainerController;

    @Before
    public void setUp() throws Exception {
        agentContainerController = new AgentContainerController();
        mockMvc = MockMvcBuilders.standaloneSetup(agentContainerController).build();
    }

    @Test
    public void testDeploy() throws Exception {

    }

    @Test
    public void testRegisterServiceWithDifferentRequestTypes() throws Exception {
        agentContainerController.entrypoints.put("SampleAgentName", new ArrayList<>());
        for (HttpMethod httpMethod : HttpMethod.values()) {
            MockHttpServletRequestBuilder request = request(httpMethod, "/jarrace/service/register/SampleAgentName")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\"agentName\":\"SampleAgentName\", \"path\":\"SamplePath\"}");
            ResultActions perform = mockMvc.perform(request);

            if (HttpMethod.POST.equals(httpMethod) || HttpMethod.OPTIONS.equals(httpMethod) || HttpMethod.TRACE.equals(httpMethod)) {
                perform.andExpect(status().isOk());
            } else {
                perform.andExpect(status().isMethodNotAllowed());
            }
        }
    }

    @Test
    public void testRegisterServiceWrongMediaType() throws Exception {
        agentContainerController.entrypoints.put("SampleAgentName", new ArrayList<>());
        MockHttpServletRequestBuilder request = post("/jarrace/service/register/SampleAgentName")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content("{\"agentName\":\"SampleAgentName\", \"path\":\"SamplePath\"}");
        mockMvc.perform(request)
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void testRegisterServiceWithWrongContent() throws Throwable {
        try {
            agentContainerController.entrypoints.put("SampleAgentName", new ArrayList<>());
            MockHttpServletRequestBuilder request = post("/jarrace/service/register/SampleAgentName")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\"path\":\"SamplePath\"}");
            mockMvc.perform(request)
                    .andExpect(status().isInternalServerError());
        } catch (NestedServletException exception) {
            Assert.assertEquals(exception.getRootCause().getClass(), IllegalArgumentException.class);
        }

        try {
            agentContainerController.entrypoints.put("SampleAgentName", new ArrayList<>());
            MockHttpServletRequestBuilder request = post("/jarrace/service/register/SampleAgentName")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content("{\"agentName\":\"SampleAgentName\"}");
            mockMvc.perform(request)
                    .andExpect(status().isInternalServerError());
        } catch (NestedServletException exception) {
            Assert.assertEquals(exception.getRootCause().getClass(), IllegalArgumentException.class);
        }
    }

    @Test
    public void testListContainersWithDifferentRequestTypes() throws Exception {
        for (HttpMethod httpMethod : HttpMethod.values()) {
            ResultActions perform = mockMvc.perform(request(httpMethod, "/jarrace/container/list"));
            if (HttpMethod.GET.equals(httpMethod) || HttpMethod.OPTIONS.equals(httpMethod) || HttpMethod.TRACE.equals(httpMethod)) {
                perform.andExpect(status().isOk());
            } else {
                perform.andExpect(status().isMethodNotAllowed());
            }
        }
    }

    @Test
    public void testListContainersWithNoContainers() throws Exception {
        mockMvc.perform(get("/jarrace/container/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testListContainersWithMultipleContainers() throws Exception {
        for (int i = 1; i <= 3; ++i) {
            agentContainerController.entrypoints.put("SampleContainer" + i, new ArrayList<>());
            mockMvc.perform(get("/jarrace/container/list"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$", hasSize(i)));
        }
    }

    @Test
    public void testListServicesWithNonexistentOrEmptyContainer() throws Exception {
        agentContainerController.entrypoints.put("SampleContainer", new ArrayList<>());
        mockMvc.perform(get("/jarrace/NonexistantContainer/entrypoint/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/jarrace/SampleContainer/entrypoint/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testListServicesWithOneOrMoreNonEmptyContainer() throws Exception {
        agentContainerController.entrypoints.put("SampleContainer",
                Collections.singletonList(new Entrypoint("Agent1", "Path1")));
        mockMvc.perform(get("/jarrace/SampleContainer/entrypoint/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        agentContainerController.entrypoints.put("SampleContainer1",
                Arrays.asList(new Entrypoint("Service1", "Path1"), new Entrypoint("Service2", "Path2")));
        mockMvc.perform(get("/jarrace/SampleContainer1/entrypoint/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

    }

    @Test
    public void testRunService() throws Exception {

    }
}