package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;


import java.util.List;

import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.PolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-19T14:57:15.952+05:30")
public class PoliciesApiServiceImpl extends PoliciesApiService {
    private static final Logger log = LoggerFactory.getLogger(PoliciesApiService.class);

    @Override
    public Response policiesTierLevelDelete(String tierName, String tierLevel, String ifMatch, String ifUnmodifiedSince,
                                            String minorVersion) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    /**
     * Retrieve a list of tiers for a particular tier level
     *
     * @param tierLevel Tier level
     * @param limit maximum number of tiers to return
     * @param offset starting position of the pagination
     * @param accept accept header value
     * @param ifNoneMatch If-Non-Match header value
     * @param minorVersion minor version
     * @return A list of qualifying tiers
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response policiesTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
                                         String ifNoneMatch, String minorVersion) throws NotFoundException {
        log.info("Received Policy GET request with tierLevel = " + tierLevel + ", limit = " + limit);

        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<Policy> policies = apiMgtAdminService.getAllPoliciesByLevel(tierLevel);
            List<TierDTO> tiers = PolicyMappingUtil.fromPoliciesToDTOs(policies);
            return Response.ok().entity(tiers).build();
        } catch (APIManagementException e) {
            String msg = "Error occurred while retrieving Policy";
            RestApiUtil.handleInternalServerError(msg, e, log);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(msg, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Add a new policy
     * 
     * @param body Details of the policy to be added 
     * @param tierLevel Tier level
     * @param contentType Content-Type header
     * @param minorVersion minor version
     * @return Newly added policy as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response policiesTierLevelPost(TierDTO body, String tierLevel, String contentType, String minorVersion)
            throws NotFoundException {
        log.info("Received Policy POST request " + body + " with tierLevel = " + tierLevel);

        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Policy policy = PolicyMappingUtil.toPolicy(tierLevel, body);
            apiMgtAdminService.addPolicy(tierLevel, policy);
            return Response.status(Response.Status.CREATED).entity(policy).build();
        } catch (APIManagementException e) {
            String msg = "Error occurred while adding Policy ";
            RestApiUtil.handleInternalServerError(msg, e, log);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(msg, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response policiesTierLevelPut(String tierName, TierDTO body, String tierLevel, String contentType,
                                         String ifMatch, String ifUnmodifiedSince, String minorVersion)
            throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response policiesUpdatePermissionPost(String tierName, String tierLevel, String ifMatch,
                                                 String ifUnmodifiedSince, String minorVersion,
                                                 TierPermissionDTO permissions) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
