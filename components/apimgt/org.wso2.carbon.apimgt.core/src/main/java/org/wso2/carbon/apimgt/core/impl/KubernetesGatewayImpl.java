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
package org.wso2.carbon.apimgt.core.impl;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.ContainerBasedGatewayGenerator;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.kernel.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;


/**
 * This is responsible to handle the auto-created gateways in container Management system
 * Current supported CMS : Kubernetes, Openshift
 */
public class KubernetesGatewayImpl implements ContainerBasedGatewayGenerator {

    private static final Logger log = LoggerFactory.getLogger(DefaultIdentityProviderImpl.class);

    private APIMConfigurations apimConfigurations = new APIMConfigurations();
    private String masterURL = apimConfigurations.getContainerGatewayConfigs().getMasterURL();
    private String carbonHome = System.getProperty(Constants.CARBON_HOME);

    private String writeTemplateToFile(String template, String filename) throws GatewayException {

        String fileLocation = carbonHome + File.separator + "deployments" + File.separator + filename + ".yaml";
        File file = new File(fileLocation);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
             PrintWriter printWriter = new PrintWriter(writer)) {

            // check if file exist, otherwise create the file before writing
            if (!file.exists()) {
                printWriter.println(template);
            }
        } catch (UnsupportedEncodingException e) {
            throw new GatewayException("Named Encoding (UTF-8) is not supported",
                    ExceptionCodes.CONTENT_ENCODING_NOT_SUPPORTED);
        } catch (FileNotFoundException e) {
            throw new GatewayException("Template file Cannot be found in the location :  " + fileLocation,
                    ExceptionCodes.FILE_NOT_FOUND_IN_LOCATION);
        } catch (IOException e) {
            throw new GatewayException("Error occurred while writing the template to the file",
                    ExceptionCodes.TEMPLATE_FILE_EXCEPTION);
        }
        return fileLocation;
    }

    @Override
    public String[] createKubernetesService(String serviceTemplate, String serviceName, String namespace)
            throws GatewayException {

        String[] accessURLs = new String[0];
        
        if (masterURL != null) {
            
            KubernetesClient kubernetesClient;
            OpenShiftClient client = null;
            List<HasMetadata> resources = null;
            String fileLocation = writeTemplateToFile(serviceTemplate, serviceName);

            try (FileInputStream fileInputStream = new FileInputStream(fileLocation)) {

                Config config = new ConfigBuilder().build();
                kubernetesClient = new DefaultKubernetesClient(config);
                client = kubernetesClient.adapt(OpenShiftClient.class);

               resources =
                        client.load(fileInputStream).get();
               if (resources.get(0) == null) {
                   client.close();
                   throw new GatewayException("No resources loaded from file: " + fileLocation,
                            ExceptionCodes.NO_RESOURCE_LOADED_FROM_FILE);

               }
               HasMetadata resource = resources.get(0);
               if (resource instanceof Service) {

             //      log.info("Creating Service in namespace " + namespace);
                   Service result = client.services().inNamespace(namespace).create((Service) resource);
              //     log.info("Created Service : " + result.getMetadata().getName());

                    
                   //todo : return the Https and https URLs form here as an array.
                   //mock
                   accessURLs[0] = "https://mygateway:9092";
                   accessURLs[1] = "http://mygateway:9090";

               } else {
              //     log.error("Loaded resource is not a Service! " + resource);
                   throw new GatewayException("Loaded Resource is not a Kubernetes Service ! " + resource,
                            ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);

               }
            } catch (KubernetesClientException | FileNotFoundException e) {

                throw new GatewayException("Client cannot load the file for service " + serviceName + "as an " +
                        "InputStream", ExceptionCodes.TEMPLATE_LOAD_EXCEPTION);

            } catch (IOException e) {
             //   log.error("Error occurred while writing the template to the file " + e);
                throw new GatewayException("Error occurred while writing the template to the file",
                        ExceptionCodes.TEMPLATE_FILE_EXCEPTION);
            }
        }
        return accessURLs;
    }

    @Override
    public void createKubernetesDeployment(String deploymentTemplate, String deploymentName, String namespace)
            throws GatewayException {
        //String fileLocation = null;


        if (masterURL != null) {
            KubernetesClient kubernetesClient;
            String fileLocation = writeTemplateToFile(deploymentTemplate, deploymentName);
            OpenShiftClient client = null;
            List<HasMetadata> resources = null;

            try (FileInputStream fileInputStream = new FileInputStream(fileLocation)) {
                Config config = new ConfigBuilder().build();
                kubernetesClient = new DefaultKubernetesClient(config);
                client = kubernetesClient.adapt(OpenShiftClient.class);

                 resources = client.load(fileInputStream).get();

                if (resources.get(0) == null) {
                    client.close();
                    throw new GatewayException("No resources loaded from file: " + fileLocation,
                            ExceptionCodes.NO_RESOURCE_LOADED_FROM_FILE);
                }
                HasMetadata resource = resources.get(0);
                if (resource instanceof Deployment) {


                    Deployment result =
                            client.extensions().deployments().inNamespace(namespace).create((Deployment) resource);
                 //   log.info("Created deployment : ", result.getMetadata().getName());

                } else {
                    throw new GatewayException("Loaded Resource is not a Kubernetes Deployment ! " + resource,
                            ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);
                }

            } catch (KubernetesClientException | FileNotFoundException e) {
                throw new GatewayException("Client cannot load the file for deployment " + deploymentName + "as an " +
                        "InputStream", ExceptionCodes.TEMPLATE_LOAD_EXCEPTION);

            } catch (IOException e) {
                log.error("No File Found in " + fileLocation + "  :  " + e);
                throw new GatewayException("No File Found in  : " + fileLocation,
                        ExceptionCodes.NO_RESOURCE_LOADED_FROM_FILE);
            }  finally {
                if (client != null) {

                    client.close();

                }
            }
        }

    }
}
