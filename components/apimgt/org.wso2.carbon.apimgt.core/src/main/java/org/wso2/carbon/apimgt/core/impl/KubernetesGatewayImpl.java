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

    private String writeKubeConfigToFile(String template, String apiId, String filename) throws GatewayException {

        String fileLocation = carbonHome + File.separator + apiId +  File.separator +
                filename + ".yaml";
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

    public static boolean deleteKubeConfig(File directory) {
        if (directory.isDirectory()) {
            File[] children = directory.listFiles();
            boolean success = false;
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    success = deleteKubeConfig(children[i]);
                }
            }
            if (!success) {
                return false;
            }
        }
        // either file or an empty directory
        log.info("removing file or directory : " + directory.getName());
        return directory.delete();

    }

    @Override
    public void createContainerBasedService(String serviceTemplate, String apiId, String serviceName, String namespace)
            throws GatewayException {

  //      String[] accessURLs = new String[0];
        
        if (masterURL != null) {

            List<HasMetadata> resources;

            Config config = new ConfigBuilder().build();
            KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
            String fileLocation = writeKubeConfigToFile(serviceTemplate, apiId, serviceName);

            try (FileInputStream fileInputStream = new FileInputStream(fileLocation);
                 OpenShiftClient client = kubernetesClient.adapt(OpenShiftClient.class)) {

               resources = client.load(fileInputStream).get();
               if (resources.get(0) == null) {
                   throw new GatewayException("No resources loaded from file: " + fileLocation,
                            ExceptionCodes.NO_RESOURCE_LOADED_FROM_FILE);

               }
               HasMetadata resource = resources.get(0);
               if (resource instanceof Service) {

                   Service result = client.services().inNamespace(namespace).create((Service) resource);
                   log.info("Created Service : " + result.getMetadata().getName());

                    
                   //todo : return the Https and https URLs form here as an array.
                   // todo : check how we can get these access URLs at label creation.
                   //mock
             //      accessURLs[0] = "https://mygateway:9092";
              //     accessURLs[1] = "http://mygateway:9090";

               } else {
                   throw new GatewayException("Loaded Resource is not a Kubernetes Service ! " + resource,
                            ExceptionCodes.LOADED_RESOURCE_IS_NOT_VALID);

               }
            } catch (KubernetesClientException | FileNotFoundException e) {
                throw new GatewayException("Client cannot load the file for service " + serviceName + "as an " +
                        "InputStream", ExceptionCodes.TEMPLATE_LOAD_EXCEPTION);

            } catch (IOException e) {
                throw new GatewayException("Error occurred while writing the template to the file",
                        ExceptionCodes.TEMPLATE_FILE_EXCEPTION);
            }
        }
    }

    @Override
    public void createContainerBasedDeployment(String deploymentTemplate, String apiId, String deploymentName,
                                        String namespace) throws GatewayException {

        if (masterURL != null) {
            Config config = new ConfigBuilder().build();
            KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
            String fileLocation = writeKubeConfigToFile(deploymentTemplate, apiId, deploymentName);
            List<HasMetadata> resources;

            try (FileInputStream fileInputStream = new FileInputStream(fileLocation);
                 OpenShiftClient client = kubernetesClient.adapt(OpenShiftClient.class)) {

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
                    log.info("Created deployment : ", result.getMetadata().getName());

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
            }
        }
    }

    @Override
    public void removeContainerBasedGateway(String label, String apiId) throws GatewayException {

        String directory = carbonHome + File.separator + "deployments" + File.separator + apiId;

        if (masterURL != null) {
            Config config = new ConfigBuilder().build();
            KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);

            try (OpenShiftClient client = kubernetesClient.adapt(OpenShiftClient.class)) {

                // Deleting the Service----------------
                // cannot get the service by namespace as at this time user may have changed the namespace in configs.
                // Therefore deleting it checking from all namespace

                // This deletes the active-mq service as well.
                client.services().inAnyNamespace().withLabel(label).delete();

                //Deleting Pods - this deletes the active-mq pod as well.
                client.pods().inAnyNamespace().withLabel(label).delete();

                //remove the configuration files from the pack as well
              // When removing the gateway, we need to remove the gateway deployment related to the particular ID
                deleteKubeConfig(new File(directory));

                log.info("Completed Deleting the Gateway and active-mq service from Container");
            } catch (KubernetesClientException e) {
                throw new GatewayException("Error in Removing the Container based Gateway Resources",
                        ExceptionCodes.CONTAINER_GATEWAY_REMOVAL_FAILED);

            }
        }
    }
}
