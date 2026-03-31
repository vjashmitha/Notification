package org.Notification.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.Notification.model.InAppNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InAppRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    // ✅ SAVE
    public void save(InAppNotification notif) {
        dynamoDBMapper.save(notif);
    }

    // ✅ GET BY USER
    public List<InAppNotification> getByUserId(String userId) {

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":userId", new AttributeValue().withS(userId));

        DynamoDBQueryExpression<InAppNotification> query =
                new DynamoDBQueryExpression<InAppNotification>()
                        .withIndexName("userId-index") // ⚠️ GSI needed
                        .withConsistentRead(false)
                        .withKeyConditionExpression("userId = :userId")
                        .withExpressionAttributeValues(values);

        return dynamoDBMapper.query(InAppNotification.class, query);
    }

    // ✅ GET UNREAD
    public List<InAppNotification> getUnread(String userId) {

        List<InAppNotification> all = getByUserId(userId);
        List<InAppNotification> unread = new ArrayList<>();

        for (InAppNotification n : all) {
            if (Boolean.FALSE.equals(n.getIsRead())) {
                unread.add(n);
            }
        }

        return unread;
    }

    // ✅ DELETE
    public void delete(InAppNotification notif) {
        dynamoDBMapper.delete(notif);
    }

    public int findByUserId(String userId) {
        return 0;
    }

    public Optional<Object> findById(String id) {
        return Optional.empty();
    }

    public void deleteById(String id) {
    }

    public Collection<Object> findByUserIdAndIsRead(String userId, boolean b) {
        return List.of();
    }
}