package server.messagehandler;

import java.util.List;

import message.Command;
import message.Message;
import message.MessageGenerator;
import server.ServerData;
import server.ServerTCPThread;

public class RegisterHandler implements ServerMessageHandler {

	@Override
	public Message handle(Message m, ServerData data, ServerTCPThread clientThread) {
		String userName = m.getAttributes().get("name");
		String userPasswort = m.getAttributes().get("passwort");
		
		if (data.usernameAvailable(userName)) {
			data.registerUser(userName, userPasswort);
			
			clientThread.setClientName(userName);
			data.addTCPRoutingInfo(userName, clientThread);
			data.addActiveUser(userName);
			
			
			//if current user was registered or logged in, send him all active user
			List<String> activeUserList = data.getActiveUser();
			System.out.println("activeUser: " + activeUserList);
			
			for(String s : activeUserList) {
				if(s != null) {
					if(!userName.equals(s)) {		//damit eigener Name nicht angezeigt wird
						System.out.println("user: " + s);
						Message newUserMessage = MessageGenerator.userOnline(s);
						clientThread.sendMessage(newUserMessage);
					}
				}
			}
			
			//if current was registered, send other users current user's status
			Message newUserMessage = MessageGenerator.userOnline(userName);
			data.getRoutingTableTCP().values()
				.stream()
				.filter(u -> !u.getClientName().equals(userName))
				.forEach(u -> u.sendMessage(newUserMessage));
			
			return new Message(Command.REGISTER_ACCEPTED);
			
		} else {
			return new Message(Command.REGISTER_DECLINED);
		}
	}

}
