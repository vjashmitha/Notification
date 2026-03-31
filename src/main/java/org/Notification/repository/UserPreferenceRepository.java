package org.Notification.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import org.Notification.model.UserPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserPreferenceRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    // ✅ SAVE
    public void save(UserPreference pref) {
        dynamoDBMapper.save(pref);
    }

    // ✅ GET BY USER ID
    public UserPreference getByUserId(String userId) {
        return dynamoDBMapper.load(UserPreference.class, userId);
    }

    public UserPreference findByUserId(String userId) {
        return null;
    }
}