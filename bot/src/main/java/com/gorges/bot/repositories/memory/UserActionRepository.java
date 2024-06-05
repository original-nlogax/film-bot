package com.gorges.bot.repositories.memory;

import com.gorges.bot.models.domain.UserAction;
import org.apache.commons.lang3.SerializationUtils;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserActionRepository {

    private final Map<Long, UserAction> usersActions = new ConcurrentHashMap<>();

    public UserAction findByChatId(Long chatId) {
        UserAction userAction = usersActions.get(chatId);
        return SerializationUtils.clone(userAction);
    }

    public void updateByChatId(Long chatId, UserAction userAction) {
        usersActions.put(chatId, SerializationUtils.clone(userAction));
    }

    public void deleteByChatId(Long chatId) {
        usersActions.remove(chatId);
    }

}
