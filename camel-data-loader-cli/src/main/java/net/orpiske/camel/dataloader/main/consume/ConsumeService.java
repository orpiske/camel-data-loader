/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.orpiske.camel.dataloader.main.consume;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// api/consume/dynamic/cli.org/1

@Path("/api/consume")
public interface ConsumeService {

    @POST
    @Produces({MediaType.TEXT_PLAIN})
    @Consumes({MediaType.TEXT_PLAIN})
    @Path("/text/dynamic/{source}/{id}")
    String consumeTextDynamic(@PathParam("source") String source, @PathParam("id") String id, String data);


    @POST
    @Produces({MediaType.TEXT_PLAIN})
    @Consumes({MediaType.TEXT_PLAIN})
    @Path("/text/static/")
    String consumeTextStatic(String data);


    @POST
    @Produces({MediaType.TEXT_PLAIN})
    @Consumes({MediaType.APPLICATION_OCTET_STREAM})
    @Path("/pdf/static/")
    String consumePdfStatic(@QueryParam("remove-pages") String removePages, @QueryParam("splitter-name") String splitterName, byte[] data);

    @POST
    @Produces({MediaType.TEXT_PLAIN})
    @Consumes({MediaType.TEXT_PLAIN})
    @Path("/file/static/")
    String consumeFileStatic(@QueryParam("splitter-name") String splitterName, String data);
}
