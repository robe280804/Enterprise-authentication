package com.roberto_sodini.authentication.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String key;

    @Value("${jwt.expiration.access_token}")
    private Long shortExpiration;

    @Value("${jwt.expiration.refresh_token}")
    private Long longExpiration;



    private Key convertKey(){
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String createAccessToken(Map<String, Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + shortExpiration))
                .signWith(convertKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String createRefreshToken(Map<String, Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + longExpiration))
                .signWith(convertKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    ///  Se viene passato True creo un access token, altrimenti un refresh token
    public String generateToken(boolean isAccess, UUID userId, String email, Collection<? extends GrantedAuthority> authorities, AuthProvider provider){
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("sub", userId);
        claims.put("email", email);
        claims.put("provider", provider);

        if (isAccess){
            return createAccessToken(claims);
        }
        return createRefreshToken(claims);
    }

    public Claims estraiAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(convertKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String estraiUserId(String token) {
        return estraiAllClaims(token).getSubject();
    }

    public String estraiEmail(String token) {
        return estraiAllClaims(token).get("email", String.class);
    }

    /*
    public List<String> estraiAuthorities(String token) {
        return estraiAllClaims(token).get("authorities", List.class);
    }
    */

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = estraiEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Date estraiExpiration(String token){
        return  estraiAllClaims(token).getExpiration();
    }

    private boolean isTokenExpired(String token){
        return estraiExpiration(token).before(new Date());
    }
}
