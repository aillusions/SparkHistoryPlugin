package org.aillusions;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.spark.ChatManager;
import org.jivesoftware.spark.SparkManager;
import org.jivesoftware.spark.plugin.Plugin;
import org.jivesoftware.spark.ui.ChatRoom;
import org.jivesoftware.spark.ui.MessageFilter;

public class HistoryLogger implements Plugin {
	private OutputStreamWriter logStream;
	private final String sfxDmainName = "@conference.jabber.sfd.net";
	private final String epamDomainName = "@sfd.jabber.epam.com";

	public boolean canShutDown() {
		return true;
	}

	private String getCurrentDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm");
		Date date = new Date();
		return dateFormat.format(date);
	}

	private String generateLogLine(ChatRoom room, Message message) {
		StringBuffer sb = new StringBuffer();
		sb.append(getCurrentDateTime() + " ");
		if(message.getFrom() != null){
			sb.append("[from " + message.getFrom().replace(sfxDmainName, "").replace(epamDomainName, "") + "] ");
		}else{
			sb.append("[to " + room.getRoomTitle() + "] ");
		}
		sb.append(message.getBody());
		sb.append("\r\n");		
		return  sb.toString();

	}

	public void initialize() {

		String logFilePath = System.getenv().get("HOMEPATH") + "\\Desktop\\spark_history.log";

		try {
			logStream = new OutputStreamWriter(new FileOutputStream(
					logFilePath, true));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		ChatManager chatManager = SparkManager.getChatManager();
		MessageFilter messageFilter = new MessageFilter() {

			public void filterOutgoing(ChatRoom room, Message message) {
				try {
					logStream.write("-> " + generateLogLine(room, message));
					logStream.flush();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void filterIncoming(ChatRoom room, Message message) {
				try {
					logStream.write("<- " + generateLogLine(room, message));
					logStream.flush();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		};

		chatManager.addMessageFilter(messageFilter);
	}

	public void shutdown() {
		try {
			logStream.flush();
			logStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void uninstall() {

	}

}