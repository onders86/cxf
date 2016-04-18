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

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.SseContext;
import javax.ws.rs.sse.SseEventOutput;

@Path("/api/bookstore")
public class BookStore {
    private static OutboundSseEvent createStatsEvent(final OutboundSseEvent.Builder builder, final int eventId) {
        return builder
            .id(Integer.toString(eventId))
            .data(Book.class, new Book("New Book #" + eventId, eventId))
            .mediaType(MediaType.APPLICATION_JSON_TYPE)
            .build();
    }
    
    @GET
    @Path("sse/{id}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public SseEventOutput forBook(@Context SseContext sseContext, @PathParam("id") final String id) {
        final SseEventOutput output = sseContext.newOutput();
        
        new Thread() {
            public void run() {
                try {
                    output.write(createStatsEvent(sseContext.newEvent().name("book"), 1));
                    Thread.sleep(200);
                    output.write(createStatsEvent(sseContext.newEvent().name("book"), 2));
                    Thread.sleep(200);
                    output.write(createStatsEvent(sseContext.newEvent().name("book"), 3));
                    Thread.sleep(200);
                    output.write(createStatsEvent(sseContext.newEvent().name("book"), 4));
                    Thread.sleep(200);
                    output.close();
                } catch (final InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return output;
    }
    
    @GET
    @Path("sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public SseEventOutput forEvent(@Context SseContext sseContext, 
            @HeaderParam(HttpHeaders.LAST_EVENT_ID_HEADER) final String lastEventId) {
        final SseEventOutput output = sseContext.newOutput();
        
        new Thread() {
            public void run() {
                try {
                    final Integer id = Integer.valueOf(lastEventId);
                    output.write(createStatsEvent(sseContext.newEvent().name("book"), id + 1));
                    Thread.sleep(100);
                    output.write(createStatsEvent(sseContext.newEvent().name("book"), id + 2));
                    Thread.sleep(100);
                    output.close();
                } catch (final InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return output;
    }

}
