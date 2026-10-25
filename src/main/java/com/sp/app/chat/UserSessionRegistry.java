package com.sp.app.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class UserSessionRegistry {
	private static final Map<Long, String> onlineUsers = new ConcurrentHashMap<>();
	
	public void add(Long member_id, String name) {
		onlineUsers.put(member_id, name);
	}
	
	public void remove(Long member_id) {
		onlineUsers.remove(member_id);
	}
	
	public Map<Long, String> getAllOnlineusers() {
		return onlineUsers;
	}
	public boolean isOnline(Long memberId) {
	    return onlineUsers.containsKey(memberId); 
	}
}
