package org.wso2.carbon.apimgt.core.template;

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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.util.ContainerBasedGatewayConstants;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

/**
 * Generate Container Based Service and Deployment using velocity template
 */
public class ContainerBasedGatewayTemplateBuilder {

    protected String cmsTemplateLocation =
            "resources" + File.separator + "template" + File.separator + "gateway_container_templates" +
                    File.separator;

    public static final String CLASS_PATH = "classpath";
    public static final String CLASS_PATH_RESOURCE_LOADER = "classpath.resource.loader.class";
    public static final String GATEWAY_LABEL = "gatewayName";
    public static final String GATEWAY_SERVICE_NAME = "gatewayServiceName";
    private static final String GATEWAY_DEPLOYMENT_NAME = "gatewayDeploymentName";
    private static final String CONTAINER_NAME = "gatewayContainerName";
    private static final String NAMESPACE = "namespace";
    private static final String IMAGE = "image";
    private static final String API_CORE_URL = "apiCoreUrl";
    private static final String BROKER_HOST_IP = "brokerHostIp";


     /**
     * Set velocity context for Gateway Service.
     *
     * @param templateValues VelocityContext template values Map
     * @return Velocity Context Object
     */
    public VelocityContext setGatewayServiceContextValues(Map<String, String> templateValues) {

        VelocityContext context = new VelocityContext();
        context.put(GATEWAY_SERVICE_NAME, templateValues.get("gatewayServiceName"));
        context.put(NAMESPACE, templateValues.get("namespace"));
        context.put(GATEWAY_LABEL, templateValues.get("gatewayLabel"));

        return context;
    }


    /**
     * Set velocity context  for Gateway Deployment.
     *
     * @param templateValues VelocityContext template values Map
     * @return Velocity Context Object
     */
    public VelocityContext setGatewayDeploymentContextValues(Map<String, String> templateValues) {

        VelocityContext context = new VelocityContext();
        context.put(GATEWAY_DEPLOYMENT_NAME, templateValues.get("gatewayDeploymentName"));
        context.put(NAMESPACE, templateValues.get("namespace"));
        context.put(GATEWAY_LABEL, templateValues.get("gatewayLabel"));
        context.put(CONTAINER_NAME, templateValues.get("gatewayContainerName"));
        context.put(IMAGE, templateValues.get("image"));
        context.put(API_CORE_URL, templateValues.get("apiGatewayUrl"));
        context.put(BROKER_HOST_IP, templateValues.get("brokerHostIp"));

        return context;
    }

    /**
     * Init velocity engine.
     *
     * @return Velocity engine for service template
     */
    public VelocityEngine initVelocityEngine() {
        VelocityEngine velocityengine = new VelocityEngine();
        velocityengine.setProperty(RuntimeConstants.RESOURCE_LOADER, CLASS_PATH);
        velocityengine.setProperty(CLASS_PATH_RESOURCE_LOADER, ClasspathResourceLoader.class.getName());
        velocityengine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new CommonsLogLogChute());
        velocityengine.init();
        return velocityengine;
    }

    /**
     * Return Gateway service template for policy level.
     *
     * @param serviceTemplateValues service template values map
     * @return Service Template as a String
     * @throws GatewayException If an error occurred when getting the Service template
     */
    public String getGatewayServiceTemplate(Map<String, String> serviceTemplateValues) throws GatewayException {

        StringWriter writer = new StringWriter();
        Template template = initVelocityEngine().getTemplate(cmsTemplateLocation +
                ContainerBasedGatewayConstants.GATEWAY_SERVICE_TEMPLATE);
        template.merge(setGatewayServiceContextValues(serviceTemplateValues), writer);
        return writer.toString();
    }

    /**
     * Return Gateway deployment template for policy level.
     *
     * @param deploymentTemplateValues service template values map
     * @return Deployment Template as a String
     */
    public String getGatewayDeploymentTemplate(Map<String, String> deploymentTemplateValues) {

        StringWriter writer = new StringWriter();
        Template template = initVelocityEngine().getTemplate(cmsTemplateLocation +
                ContainerBasedGatewayConstants.GATEWAY_DEPLOYMENT_TEMPLATE);
        template.merge(setGatewayDeploymentContextValues(deploymentTemplateValues), writer);
        return writer.toString();
    }

}
