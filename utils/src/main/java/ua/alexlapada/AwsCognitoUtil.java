package ua.alexlapada;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AwsCognitoUtil {
    private static final String SPACE_CHAR = "\\s";
    private static final String SPACE = " ";
    private static final String SPECIAL_CHARACTERS = "[-+'$%^]*";

    public static String getUserAttr(List<AttributeType> userAttributes, String attrName) {
        for (AttributeType attrType : userAttributes) {
            if (attrName.equals(attrType.name())) {
                return attrType.value();
            }
        }
        return null;
    }

    public static boolean isUserEmailVerified(List<AttributeType> userAttributes) {
        return "true".equals(getUserAttr(userAttributes, "email_verified"));
    }

    public static String prepareIdpName(String companyName) {
        return companyName.replaceAll(SPECIAL_CHARACTERS, "")
                          .replaceAll(SPACE, "-")
                          .replaceAll(SPACE_CHAR, "-")
                          .toLowerCase();
    }
}
