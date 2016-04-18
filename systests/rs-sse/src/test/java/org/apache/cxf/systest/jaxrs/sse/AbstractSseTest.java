/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.systest.jaxrs.sse;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;

public abstract class AbstractSseTest extends AbstractBusClientServerTestBase {
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Test
    public void testBooksStreamIsReturned() throws JsonProcessingException {
        Response r = createWebClient("/rest/api/bookstore/sse/100").get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        
        final String response = r.readEntity(String.class);
        assertThat(response, containsString("id: 1"));
        assertThat(response, containsString("data: " + toJson("New Book #1", 1)));
        
        assertThat(response, containsString("id: 2"));
        assertThat(response, containsString("data: " + toJson("New Book #2", 2)));
        
        assertThat(response, containsString("id: 3"));
        assertThat(response, containsString("data: " + toJson("New Book #3", 3)));
        
        assertThat(response, containsString("id: 4"));
        assertThat(response, containsString("data: " + toJson("New Book #4", 4)));
    }
    
    @Test
    public void testBooksStreamIsReturnedFromLastEventId() throws JsonProcessingException {
        Response r = createWebClient("/rest/api/bookstore/sse")
            .header(HttpHeaders.LAST_EVENT_ID_HEADER, 150)
            .get();
        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        
        final String response = r.readEntity(String.class);
        assertThat(response, containsString("id: 151"));
        assertThat(response, containsString("data: " + toJson("New Book #151", 151)));
        
        assertThat(response, containsString("id: 152"));
        assertThat(response, containsString("data: " + toJson("New Book #152", 152)));
    }

    private String toJson(final String name, final Integer id) throws JsonProcessingException {
        return mapper.writeValueAsString(new Book(name, id));
    }
    
    protected WebClient createWebClient(final String url) {
        final List< ? > providers = Arrays.asList(new JacksonJsonProvider());
        
        final WebClient wc = WebClient
            .create("http://localhost:" + getPort() + url, providers)
            .accept(MediaType.SERVER_SENT_EVENTS);
        
        WebClient.getConfig(wc).getHttpConduit().getClient().setReceiveTimeout(10000000L);
        return wc;
    }
    
    protected abstract int getPort();
}
