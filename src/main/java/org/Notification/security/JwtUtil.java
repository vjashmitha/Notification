package org.Notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "k8Jd9wQ2x+4aB3d1FJtL8vZ5X0yQ1V7n2gHqM4sP1tE=";

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String getRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String getEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }
}
