package com.multimedia.centercontroller;

import android.util.Log;

import com.mmclass.libsiren.LibSiren;

public class CommandManager {

	private static CommandManager sIntance = new CommandManager();
	private static LibSiren mLibSiren;
	private static String mSeatNo;
	private static boolean micFlag=true;

	private CommandManager() {

	}

	public synchronized CommandManager getInstance() {
		if (sIntance == null) {
			sIntance = new CommandManager();
		}
		return sIntance;
	}

	public static void SetSeatNo(String seatNo) {
		mSeatNo = seatNo;
	}

	public static void SetLibSiren(LibSiren libSiren) {
		mLibSiren = libSiren;
	}

	public static void sendSetSeatMessage() {
		String setSeatMsg = "type:" + CommonUtil.getSeatNo() + ","
				+ "receiver:all" + "," + "mode:" + Command.Modes.MODE_GLOBAL
				+ "," + "command:" + Command.COMMANDS.COMMAND_SET_SEAT + ","
				+ "group:null" + "," + "param:null";

		mLibSiren.sendMessage(Command.formatMessage(setSeatMsg));
	}

	public static void sendSetAllCallMessage() {
		CommandManager.leaveGroup("");
		CommandManager.joinGroup("230.1.2.3");
		String setOnlineMsg = "type:" + mSeatNo + "," + "receiver:all" + ","
				+ "mode:" + Command.Modes.MODE_TEACH + "," + "command:"
				+ Command.COMMANDS.COMMAND_TEACH_ALL_CALLS + "," + "group:null"
				+ "," + "param:null";

		mLibSiren.sendMessage(Command.formatMessage(setOnlineMsg));
	}

	public static void sendSetOnlineMessage() {
		String setOnlineMsg = "type:cmd" + "," + "receiver:all"
				+ "," + "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
				+ Command.COMMANDS.COMMAND_GLOBAL_ONLINE + "," + "group:null"
				+ "," + "param:null";

		mLibSiren.sendMessage(Command.formatMessage(setOnlineMsg));
	}
	
	   public static void sendSetVGAMessage(String params) {
	        String setVGAMsg = "type:cmd" + "," + "receiver:all"
	                + "," + "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
	                + Command.COMMANDS.COMMAND_GLOBAL_BROADCAST + "," + "group:g1"
	                + "," + "param:"+params;
	        mLibSiren.sendMessage(Command.formatMessage(setVGAMsg));
	    }
	   
       public static void sendSetSoundMessage(String sound) {
           String setSoundMsg = "type:cmd" + "," + "receiver:teacher"
                   + "," + "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
                   + Command.COMMANDS.COMMAND_SOUND + "," + "group:g1"
                   + "," + "param:"+sound;
           mLibSiren.sendMessage(Command.formatMessage(setSoundMsg));
       }
       
       public static void sendSetMicMessage(String param) {
           String setMicMsg = "type:cmd" + "," + "receiver:teacher"
                   + "," + "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
                   + Command.COMMANDS.COMMAND_MIC + "," + "group:g1"
                   + "," + "param:"+param;
           mLibSiren.sendMessage(Command.formatMessage(setMicMsg));
       }
       
       public static void sendSetSpeakerMessage(String params) {
           String setSpeakerAMsg = "type:cmd" + "," + "receiver:teacher"
                   + "," + "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
                   + Command.COMMANDS.COMMAND_SPEAKER + "," + "group:g1"
                   + "," + "param:"+params;
           mLibSiren.sendMessage(Command.formatMessage(setSpeakerAMsg));
       }
       public static void sendSetHeadsetMessage(String params) {
           String setHeadsetMsg = "type:cmd" + "," + "receiver:teacher"
                   + "," + "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
                   + Command.COMMANDS.COMMAND_HEADSET + "," + "group:g1"
                   + "," + "param:"+params;
           mLibSiren.sendMessage(Command.formatMessage(setHeadsetMsg));
       }
       
       //local/remote
       public static void sendBroadcastMessage(String params) {
           String setBroadcastMsg = "type:cmd" + "," + "receiver:all"
                   + "," + "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
                   + Command.COMMANDS.COMMAND_GLOBAL_BROADCAST + "," + "group:g1"
                   + "," + "param:"+params;
           mLibSiren.sendMessage(Command.formatMessage(setBroadcastMsg));
       }   


	// demonstration
	public static void setDemonstration(String addr) {
		mLibSiren.satJoin(addr, 6000);
	}

	// intercom
	public static void setIntercom(String addr) {
		mLibSiren.satJoin(addr, 6000);
	}

	// monitor
	public static void setMonitor(String addr) {
		mLibSiren.satJoin(addr, 6000);
	}

	// dictation
	public static void sendDictationMessage(String content) {

		String dictationMsg = "type:" + mSeatNo + "," + "receiver:all" + ","
				+ "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
				+ Command.COMMANDS.COMMAND_TEACH_DICTATION + "," + "group:null"
				+ "," + "param:" + content;

		// content;
		mLibSiren.sendMessage(Command.formatMessage(dictationMsg));
	}

	// test
	public static void sendTestMessage(String content) {

		String testMsg = "type:" + mSeatNo + "," + "receiver:all" + ","
				+ "mode:" + Command.Modes.MODE_GLOBAL + "," + "command:"
				+ Command.COMMANDS.COMMAND_TEACH_TEST + "," + "group:null"
				+ "," + "param:" + content;

		mLibSiren.sendMessage(Command.formatMessage(testMsg));
	}

	// translate
	public static void setTranslate(String addr) {
		mLibSiren.satJoin(addr, 6000);
	}

	// group
	// translate
	public static void joinGroup(String addr) {
		Log.d("join", "join group " + addr);
		mLibSiren.satJoin(addr, 6000);
	}

	public static void leaveGroup(String addr) {
		Log.d("leave", "leave group " + addr);
		mLibSiren.satLeave(addr);
	}

	// exam
	public static void sendStandardExamMessage(String content) {
		String examMsg = "{" + "\"" + "type" + "\"" + ":" + "\"" + mSeatNo
				+ "\"" + "," + "\"" + "receiver" + "\"" + ":" + "\"" + "all"
				+ "\"" + "," + "\"" + "mode" + "\"" + ":" + "\""
				+ Command.Modes.MODE_GLOBAL + "\"" + "," + "\"" + "command"
				+ "\"" + ":" + "\"" + Command.COMMANDS.COMMAND_EXAM_STANDARD
				+ "\"" + "," + "\"" + "group" + "\"" + ":" + "\"" + "all"
				+ "\"" + "," + "\"" + "param" + "\"" + ":" + "\"" + content
				+ "\"" + "}";
		mLibSiren.sendMessage(examMsg);
	}

	// exam
	public static void sendIPcallMessage(String receiver) {

		String IpCallMsg = "type:cmd" + "," + "receiver:" + receiver + ","
				+ "mode:" + Command.Modes.MODE_SELF_STUDY + "," + "command:"
				+ Command.COMMANDS.COMMAND_SELF_IP_CALL + "," + "group:g100"
				+ "," + "param:" + CommonUtil.getSeatNo();

		mLibSiren.sendMessage(Command.formatMessage(IpCallMsg));
	}

	// record
	public static void mp3LameInit(int channel, int sampleRate, int brate) {
		mLibSiren.mp3LameInit(channel, sampleRate, brate);
	}

	// record
	public static void mp3LameDestroy() {
		mLibSiren.mp3LameDestroy();
	}

	public static byte[] mp3LameEncode(byte[] buffer, int len) {
		return mLibSiren.mp3LameEncode(buffer, len);
	}

	// Center controller command
	// switch teach model
	public static void sendSwitchTeachModel() {

		String switchTeachMsg = "type:cmd" + "," + "receiver:" + "all" + ","
				+ "mode:" + Command.Modes.MODE_TEACH + "," + "command:"
				+ Command.COMMANDS.COMMAND_TEACH_ALL_CALLS + "," + "group:g1"
				+ "," + "param:null";

		mLibSiren.sendMessage(Command.formatMessage(switchTeachMsg));
	}

	// switch group model
	public static void sendSwitchGroupModel() {

		String switchGroupMsg = "type:cmd" + "," + "receiver:" + "all" + ","
				+ "mode:" + Command.Modes.MODE_GROUP + "," + "command:"
				+ Command.COMMANDS.COMMAND_GROUP + "," + "group:g1" + ","
				+ "param:null";

		mLibSiren.sendMessage(Command.formatMessage(switchGroupMsg));
	}

	// demonstrate
	public static void sendDemonstrationCommand(String seatNo) {

		String demonstrationMsg = "type:cmd" + "," + "receiver:" + seatNo + ","
				+ "mode:" + Command.Modes.MODE_TEACH + "," + "command:"
				+ Command.COMMANDS.COMMAND_TEACH_DEMONSTRATION + ","
				+ "group:g1" + "," + "param:null";

		mLibSiren.sendMessage(Command.formatMessage(demonstrationMsg));
	}

	// intercom
	public static void sendIntercomCommand(String seatNo) {
              CommandManager.sendAllCleanCommand();
		String intercomMsg = "type:cmd" + "," + "receiver:" + seatNo + ","
				+ "mode:" + Command.Modes.MODE_TEACH + "," + "command:"
				+ Command.COMMANDS.COMMAND_TEACH_INTERCOM + "," + "group:g1"
				+ "," + "param:null";

		mLibSiren.sendMessage(Command.formatMessage(intercomMsg));
	}

	// monitor
	public static void sendMonitorCommand(String seatNo) {

		String monitorMsg = "type:cmd" + "," + "receiver:" + seatNo + ","
				+ "mode:" + Command.Modes.MODE_TEACH + "," + "command:"
				+ Command.COMMANDS.COMMAND_TEACH_MONITOR + "," + "group:g1"
				+ "," + "param:null";

		mLibSiren.sendMessage(Command.formatMessage(monitorMsg));
	}

	// discuss
	public static void sendDiscussCommand(String seatNo, int group,
			String members) {
		String discussMsg = "{" + "\"" + "type" + "\"" + ":" + "\"" + "cmd"
				+ "\"" + "," + "\"" + "receiver" + "\"" + ":" + "\""
				+ seatNo
				+ "\""
				+ ","
				+ "\""
				+ "mode"
				+ "\""
				+ ":"
				+ "\""
				+ Command.Modes.MODE_GROUP
				+ "\""
				+ ","
				+ "\""
				+ "command"
				+ "\""
				+ ":"
				+ "\""
				+ Command.COMMANDS.COMMAND_GROUP_DISCUSS
				+ "\""
				+ ","
				+ "\""
				+ "group"
				+ "\""
				+ ":"
				+ "\""
				+ group
				+ "\""
				+ ","
				+ "\""
				+ "param"
				+ "\""
				+ ":"
				+ "\""
				+ members
				+ "\"" + "}";
		mLibSiren.sendMessage(discussMsg);
	}

	// clean
	public static void sendCleanCommand(String seatNo) {

		String monitorMsg = "type:cmd" + "," + "receiver:" + seatNo + ","
				+ "mode:" + Command.Modes.MODE_TEACH + "," + "command:"
				+ Command.COMMANDS.COMMAND_GLOBAL_CLEAN + "," + "group:g1"
				+ "," + "param:null";

		mLibSiren.sendMessage(Command.formatMessage(monitorMsg));
	}

         // all clean
		public static void sendAllCleanCommand() {

			String monitorMsg = "type:cmd" + "," + "receiver:" + "all" + ","
					+ "mode:" + Command.Modes.MODE_TEACH + "," + "command:"
					+ Command.COMMANDS.COMMAND_GLOBAL_CLEAN + "," + "group:g1"
					+ "," + "param:null";

			mLibSiren.sendMessage(Command.formatMessage(monitorMsg));
		}

	// Switch self study model
	public static void sendSwitchStudyCommand() {

		String studyMsg = "type:cmd" + "," + "receiver:" + "all" + ","
				+ "mode:" + Command.Modes.MODE_SELF_STUDY + "," + "command:"
				+ Command.COMMANDS.COMMAND_SELF_STUDY + "," + "group:g1" + ","
				+ "param:null";

		mLibSiren.sendMessage(Command.formatMessage(studyMsg));
	}

	public static void startSat() {
		if (mLibSiren != null) {
			Log.d("lisiren", "startSat()");
			mLibSiren.startSat();
		}
	}

	public static void destorySat() {
		if (mLibSiren != null) {
			Log.d("lisiren", "destorySat()");
			mLibSiren.destorySat();
		}
	}

	public static int getSpeakerStatus() {

		return mLibSiren.getSpeakerStatus();
	}

	public static int openSpeaker() {

		return mLibSiren.openSpeaker();
	}

	public static int closeSpeaker() {

		return mLibSiren.closeSpeaker();
	}
	
	public static int getLineinStatus() {

		return mLibSiren.getLineinStatus();
	}

	public static int openLinein() {

		return mLibSiren.openLinein();
	}

	public static int closeLinein() {

		return mLibSiren.closeLinein();
	}

	public static int getMicStatus() {

		return mLibSiren.getMicStatus();
	}
	
	public static boolean setMicFlag(boolean flag){
		micFlag = flag;
		return micFlag;
	}

	public static int openMic() {
		if(micFlag)
			return mLibSiren.openMic();
		else
			return 1;
	}

	public static int closeMic() {
		return mLibSiren.closeMic();
	}


	public static int getHeadsetStatus() {

		return mLibSiren.getHeadsetStatus();
	}

	public static int openHeadset() {

		return mLibSiren.openHeadset();
	}

	public static int closeHeadset() {

		return mLibSiren.closeHeadset();
	}
        
       	public static int setSound(int sound){
		return mLibSiren.setSound(sound);
	}
	
	public static int getSound(){
		return mLibSiren.getSound();
	}
      
       	public static int setToneUp(int value){
		return mLibSiren.setToneUp(value);
	}
}
