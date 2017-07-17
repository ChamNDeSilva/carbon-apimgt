/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.GatewayException;

import java.io.IOException;


/**
 * The interface used to manage the auto-created gateways in container Management system
 * Current supported CMS : Kubernetes, Openshift
 */
public interface ContainerBasedGatewayGenerator {

    /**
     * Create a Service in the Container Management System
     *
     *
     * @param gatewayServiceTemplate Definition of the Service as a String
     * @param serviceName   Name of the Service
     * @param namespace   namespace of the service
     *
     * @throws GatewayException     If there is a failure to update API in gateway
     */
    void createKubernetesService(String gatewayServiceTemplate, String serviceName, String namespace)
            throws GatewayException, IOException;

    /**
     * Create the gateway deployment in Container Management System
     *
     *
     * @param gatewayDeploymentTemplate   Definition of the Deployment as a String
     * @param deploymentName Name of the deployment
     * @param namespace Namespace of the deployment
     *
     * @throws GatewayException   If there is a failure to update API in gateway
     */
    void createKubernetesDeployment(String gatewayDeploymentTemplate, String deploymentName, String namespace)
            throws GatewayException;

    /**
     * Remove the existing gateway from Container Management System
     *
     * @param label   auto-generated label of gateway
     * @throws GatewayException   If there is a failure to update API in gateway
     */
    void removeKubernetesGateway(String label) throws GatewayException;






}


