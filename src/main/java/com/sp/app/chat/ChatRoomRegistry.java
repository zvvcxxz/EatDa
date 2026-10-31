package com.sp.app.chat;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomRegistry {

    public static class RoomInfo {
        private final String roomId;
        private final Long user1;
        private final Long user2;
        private boolean active;

        public RoomInfo(String roomId, Long user1, Long user2) {
            this.roomId = roomId;
            this.user1 = user1;
            this.user2 = user2;
            this.active = true;
        }

        public String getRoomId() { return roomId; }
        public Long getUser1() { return user1; }
        public Long getUser2() { return user2; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public Long getTargetUserId(Long currentUserId) {
            return Objects.equals(user1, currentUserId) ? user2 : user1;
        }
    }

    private final Map<String, RoomInfo> roomMap = new ConcurrentHashMap<>();
    private final Map<String, List<ChatMessage>> roomMessagesMap = new ConcurrentHashMap<>();

    public String generateRoomId(Long user1, Long user2) {
        long min = Math.min(user1, user2);
        long max = Math.max(user1, user2);
        return min + "_" + max;
    }

    public boolean isRoomActive(String roomId) {
        RoomInfo info = roomMap.get(roomId);
        return info != null && info.isActive();
    }

    //roomId로 RoomInfo 객체 직접 조회
    public RoomInfo getRoomInfo(String roomId) {
        return roomMap.get(roomId);
    }

    public void activateRoom(String roomId, Long user1, Long user2) {
        RoomInfo info = roomMap.get(roomId);
        if (info == null) {
            roomMap.put(roomId, new RoomInfo(roomId, user1, user2));
        } else {
            info.setActive(true);
        }
    }

    public void closeRoom(String roomId) {
        RoomInfo info = roomMap.get(roomId);
        if (info != null) {
            info.setActive(false);
        }
    }

    public List<RoomInfo> getActiveRoomsForUser(Long userId) {
        List<RoomInfo> list = new ArrayList<>();
        for (RoomInfo info : roomMap.values()) {
            if (info.isActive() && (Objects.equals(info.getUser1(), userId) || Objects.equals(info.getUser2(), userId))) {
                list.add(info);
            }
        }
        return list;
    }

    public void saveMessage(String roomId, ChatMessage message) {
        roomMessagesMap.putIfAbsent(roomId, Collections.synchronizedList(new ArrayList<>()));
        roomMessagesMap.get(roomId).add(message);
    }

    public List<ChatMessage> getMessages(String roomId) {
        return roomMessagesMap.getOrDefault(roomId, new ArrayList<>());
    }
}