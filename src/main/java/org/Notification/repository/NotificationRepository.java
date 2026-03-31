package org.Notification.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.Notification.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class NotificationRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    // ✅ SAVE
    public Notification save(Notification notification) {
        dynamoDBMapper.save(notification);
        return notification;
    }

    // ✅ GET BY ID
    public Notification getById(String id) {
        return dynamoDBMapper.load(Notification.class, id);
    }

    // ✅ GET BY USER ID
    public List<Notification> getByUserId(String userId) {

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":userId", new AttributeValue().withS(userId));

        DynamoDBQueryExpression<Notification> query =
                new DynamoDBQueryExpression<Notification>()
                        .withIndexName("userId-index") // ⚠️ create GSI
                        .withConsistentRead(false)
                        .withKeyConditionExpression("userId = :userId")
                        .withExpressionAttributeValues(values);

        return dynamoDBMapper.query(Notification.class, query);
    }

    // ✅ DELETE
    public void delete(Notification notification) {
        dynamoDBMapper.delete(notification);
    }

    public Iterable<Notification> findAll() {
        return null;
    }

    public List<Notification> findByUserId(String userId) {
        return List.of();
    }

    public Optional<Object> findById(String id) {
        return Optional.empty();
    }
}