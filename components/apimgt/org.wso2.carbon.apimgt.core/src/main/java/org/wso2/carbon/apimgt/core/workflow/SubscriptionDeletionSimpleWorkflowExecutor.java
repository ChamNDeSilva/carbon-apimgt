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
 */
package org.wso2.carbon.apimgt.core.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;

/**
 * Subscription deletion simple workflow
 */
public class SubscriptionDeletionSimpleWorkflowExecutor implements WorkflowExecutor {
    private static final Log log = LogFactory.getLog(SubscriptionDeletionSimpleWorkflowExecutor.class);

    /**
     * execute subscription deletion 
     */
    @Override
    public WorkflowResponse execute(Workflow workflow) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Executing Subscription deletion Workflow..");
        }

        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        // set the state to approved
        workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);
        return workflowResponse;
    }

    /**
     * complete subscription deletion 
     */
    @Override
    public WorkflowResponse complete(Workflow workflow) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Complete  Subscription deletion Workflow..");
        }

        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(workflow.getStatus());
        return workflowResponse;
    }

    /**
     * clean up pending subscription deletiion tasks
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {}

}
