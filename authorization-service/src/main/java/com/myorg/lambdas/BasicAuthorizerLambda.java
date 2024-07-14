package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.List;

/**
 * @author Aliaksei Tsvirko
 */
public class BasicAuthorizerLambda implements RequestHandler<APIGatewayCustomAuthorizerEvent, IamPolicyResponse> {
    @Override
    public IamPolicyResponse handleRequest(APIGatewayCustomAuthorizerEvent request, Context context) {
        context.getLogger().log(request.toString());

        String authorizationHeaderValue = request.getAuthorizationToken();

        if (StringUtils.isEmpty(authorizationHeaderValue)) {
            throw new RuntimeException("Unauthorized.");
        }

        String authorizationToken = authorizationHeaderValue.split(" ")[1];
        String credentials = new String(Base64.getDecoder().decode(authorizationToken));
        String user = credentials.split("=")[0];
        String password = credentials.split("=")[1];

        if (StringUtils.equals(password, System.getenv(user))) {
            return generatePolicy(user, IamPolicyResponse.ALLOW, request.getMethodArn());
        }

        return generatePolicy(user, IamPolicyResponse.DENY, request.getMethodArn());
    }

    private IamPolicyResponse generatePolicy(String principalId, String effect, String resource) {
        return IamPolicyResponse.builder()
                .withPrincipalId(principalId)
                .withPolicyDocument(IamPolicyResponse.PolicyDocument.builder()
                        .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                        .withStatement(List.of(IamPolicyResponse.Statement.builder()
                                .withEffect(effect)
                                .withAction(IamPolicyResponse.EXECUTE_API_INVOKE)
                                .withResource(List.of(resource))
                                .build()))
                        .build())
                .build();
    }
}
