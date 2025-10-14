package com.keetlo.ai.service;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.keetlo.ai.model.User;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final JdbcTemplate database;
    private final SubscriptionService subscriptionService;

    public CustomOAuth2UserService(JdbcTemplate database, SubscriptionService subscriptionService) {
        this.database = database;
        this.subscriptionService = subscriptionService;
    }

@SuppressWarnings("null")
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    Map<String, Object> attributes = oauthUser.getAttributes();

    String email = null;
    String oauthId = null;
    String firstname = "";
    String lastname = "";
    String avatarUrl = null;

    if ("google".equals(registrationId)) {
        oauthId = (String) attributes.get("sub");
        email = (String) attributes.get("email");
        firstname = (String) attributes.get("given_name");
        lastname = (String) attributes.get("family_name");
        avatarUrl = (String) attributes.get("picture");
    } else if ("github".equals(registrationId)) {
        String[] name = attributes.get("name").toString().split(" ");
        oauthId = String.valueOf(attributes.get("id"));
        email = (String) attributes.get("email"); // can be null
        firstname = name[0];
        lastname = name[1];
        avatarUrl = (String) attributes.get("avatar_url");
    }

    // Check if user exists
    Integer countOAuthIdExists = database.queryForObject("SELECT COUNT(*) FROM users WHERE " + registrationId + "_id = ?", Integer.class, oauthId);
    Integer countEmailExists =  database.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
    if (countOAuthIdExists == 0 && countEmailExists == 0) {
        String sql = "INSERT INTO users (user_id, email, " + registrationId + "_id, firstname, lastname, avatar_url, email_verified_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        User user = new User();
        String newUserId = user.createUserId();
        database.update(sql, newUserId, email, oauthId, firstname, lastname, avatarUrl);
        subscriptionService.userDefaultSubscribe(newUserId);
    } 
    else if (countOAuthIdExists == 0 && countEmailExists > 0){
        String sql = "UPDATE users SET " + registrationId + "_id=? WHERE email = ?";
        database.update(sql, oauthId, email);
    }

    return oauthUser;
}

}

