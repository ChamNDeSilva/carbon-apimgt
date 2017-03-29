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
package org.wso2.carbon.apimgt.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.authenticator.dto.ErrorDTO;
import org.wso2.carbon.apimgt.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * This class provides access token during login from store app.
 *
 */
@Path("/oauth")
public class AuthenticatorAPI implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatorAPI.class);

    /**
     * This method authenticate the user for store app.
     *
     */
    @POST
    @Path ("/token")
    @Produces (MediaType.APPLICATION_JSON)
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    public Response authenticate(
            @Context Request request, @FormDataParam ("username") String userName,
            @FormDataParam ("password") String password, @FormDataParam ("grant_type") String grantType,
            @FormDataParam ("validity_period") String validityPeriod,
            @FormDataParam ("scopes") String scopesList) {
        try {

            LoginTokenService loginTokenService = new LoginTokenService();
            AuthResponseBean authResponseBean = new AuthResponseBean();
            String appContext = AuthUtil.getAppContext(request);
            String restAPIContext = "/api/am" + appContext;
            String refToken = null;
            if (AuthenticatorConstants.REFRESH_GRANT.equals(grantType)) {
                refToken = AuthUtil.extractRefreshTokenFromHeaders(request.getHeaders());
                if (refToken == null) {
                    ErrorDTO errorDTO = new ErrorDTO();
                    errorDTO.setCode(ExceptionCodes.INVALID_AUTHORIZATION_HEADER.getErrorCode());
                    errorDTO.setMessage(ExceptionCodes.INVALID_AUTHORIZATION_HEADER.getErrorMessage());
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
                }
            }
            String tokens = loginTokenService
                    .getTokens(authResponseBean, appContext.substring(1), userName, password, grantType, refToken,
                            scopesList.split("" + " "), Long.parseLong(validityPeriod));
            String accessToken = tokens.split(":")[0];
            String refreshToken = null;
            if (tokens.split(":").length > 1) {
                refreshToken = tokens.split(":")[1];
            }

            String part1 = accessToken.substring(0, accessToken.length() / 2);
            String part2 = accessToken.substring(accessToken.length() / 2);
            NewCookie cookie = AuthUtil.cookieBuilder(AuthenticatorConstants.TOKEN_1, part1, appContext, true, false);
            NewCookie cookie2 = AuthUtil
                    .cookieBuilder(AuthenticatorConstants.TOKEN_2, part2, restAPIContext, true, true);
            NewCookie refreshTokenCookie1, refreshTokenCookie2 = null;
            if (refreshToken != null) {
                String refTokenPart1 = refreshToken.substring(0, refreshToken.length() / 2);
                String refTokenPart2 = refreshToken.substring(refreshToken.length() / 2);
                refreshTokenCookie1 = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_1, refTokenPart1, appContext, true, false);
                refreshTokenCookie2 = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_2, refTokenPart2, appContext, true, true);
                return Response.ok(authResponseBean, MediaType.APPLICATION_JSON)
                        .cookie(cookie, cookie2, refreshTokenCookie1, refreshTokenCookie2)
                        .header(AuthenticatorConstants.
                                        REFERER_HEADER,
                                (request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null && request
                                        .getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)
                                        .equals(request.getHeader(AuthenticatorConstants.REFERER_HEADER))) ?
                                        "" :
                                        request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null ?
                                                request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) :
                                                "").build();
            } else {
                return Response.ok(authResponseBean, MediaType.APPLICATION_JSON).cookie(cookie, cookie2)
                        .header(AuthenticatorConstants.
                                        REFERER_HEADER,
                                (request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null && request
                                        .getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)
                                        .equals(request.getHeader(AuthenticatorConstants.REFERER_HEADER))) ?
                                        "" :
                                        request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null ?
                                                request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) :
                                                "").build();
            }
        } catch (APIManagementException e) {

            ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);

            log.error(e.getMessage(), e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @POST
    @Produces (MediaType.APPLICATION_JSON)
    @Path ("/revoke")
    public Response logout(@Context Request request) {
        String appContext = AuthUtil.getAppContext(request);
        String restAPIContext = "/api/am" + appContext;
        return Response.ok().header("Set-Cookie",
                AuthenticatorConstants.TOKEN_2 + "=;Path=" + restAPIContext + ";Expires=Thu, 01-Jan-1970 00:00:01 GMT")
                .build();
        //TODO : Need to call revoke endpoint.
    }

}
