package ua.alexlapada;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtil {
    private static final int JWT_PAYLOAD_PART = 1;
    private static final int JWT_LENGTH = 3;

    public static String parseJwt(String token) {
        if (token == null) {
            throw new IllegalArgumentException("JWT Token can not be null.");
        }
        String[] chunks = token.split("\\.");
        if (chunks.length != JWT_LENGTH) {
            log.error("Invalid JWT Token");
            throw new AccessDeniedException("Invalid JWT Token");
        }
        return new String(Base64.getMimeDecoder().decode(chunks[JWT_PAYLOAD_PART]), StandardCharsets.UTF_8);
    }

    public static String getIdpId(String token) {
        String payload = parseJwt(token);
        JwtInfo jwtInfo;
        try {
            jwtInfo = JacksonUtil.readJson(payload, JwtInfo.class);
        } catch (IOException e) {
            log.error("Invalid JWT Token");
            throw new AccessDeniedException("Invalid JWT Token");
        }
        return jwtInfo.getSub();
    }
    
    @Data
    public static class JwtInfo {
        private String sub;
    }
}
