/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold Container Based Gateway configurations
 */
@Configuration(description = "Container Based Gateway Configurations")
public class ContainerBasedGatewayConfiguration {

    @Element(description = "Access URL of the Container Based Gateway")
    private String masterURL = "https://192.168.42.188:8443";

    @Element(description = "Namespace for the Service to be created")
    private String namespace = "default";

    @Element(description = "Name of the Docker Image of the Gateway")
    private String image = "wso2apim-gateway-3.0.0-SNAPSHOT:3.0.0";

    @Element(description = "URL of API Core")
    private String apiCoreURL = "https://localhost:9292";

    @Element(description = "Message Broker Host IP address")
    private String brokerHost = "10.100.7.106";
    // todo : change this to localhost ip address


    // todo : Add authentication configs

    public String getMasterURL() {
            return masterURL;
        }

    public String getNamespace() {
            return namespace;
        }

    public String getApiCoreURL() {
            return apiCoreURL;
        }

    public String getImage() {
        return image;
    }

    public String getBrokerHost() {
        return brokerHost;
    }



    }
