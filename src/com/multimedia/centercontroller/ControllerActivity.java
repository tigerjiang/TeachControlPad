package com.multimedia.centercontroller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mmclass.libsiren.EventHandler;
import com.mmclass.libsiren.LibSiren;
import com.mmclass.libsiren.LibSirenException;
import com.mmclass.libsiren.WeakHandler;
import com.multimedia.centercontroller.SetDialogFragment.IUpdateSeatNo;

public class ControllerActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnItemSelectedListener, OnItemClickListener,
		IUpdateSeatNo {
	private static final String TAG = ControllerActivity.class.getSimpleName();
	private LinearLayout mSwitchTeachButton, mSwitchGroupButton,
			mSwitchSelfButton;
	private LinearLayout mDemonButton, mIntercomButton, mMonitorButton;
	private ImageButton mSetButton;
	private Button mSpeakerButton, mMicrophoneButton, mHeadSetButton;
	private TextView mDeviceStateView, mTimeView, mStudentStateView,
			mErrorMsgView, mVersionView;
	private LinearLayout mGroupSum2, mGroupSum4;
	private TextView mExitBtn;
	private GridView mSeatView;
	private String mReceiver;
	private static StudentAdapter mAdapter;
	private static String mCurrentSeat;
	private static Student sStudent;
	private static List<Student> mStudentList;
	private static String mCurrentCommand;
	private static boolean sIsOnline = false;
	private RelativeLayout mCoverView;
	protected static Object sObjectLock = new Object();
	private MessageHandler mHandler;
	private static LibSiren mLibSiren;
	protected static EventHandler em = EventHandler.getInstance();
	protected static Context mBaseContext;
	protected static String mSeatNo;
	private static CommonUtil mUtil;
	private static int mRow;
	private static int mColumn;
	// group unit
	private int mGroupUnit = 2;
	private int mScreenWidth;
	private int mScreenHeight;

	private RadioGroup mRadioGroup;
	private int delayTime = 5000;// ms
	private static String mGroupAddr = "";
	private boolean mIsInternetConnected = false;
	private ConnectivityManager mConnectivityManager;
	private final NetworkStatusReceiver mNetworkReceiver = new NetworkStatusReceiver();
	private static boolean mNetworkReceiverRegistered = false;
	private Handler mTimeHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
//				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//				String str = sdf.format(new Date());
//				mTimeView.setText(str);
//				mTimeHandler.sendEmptyMessageDelayed(1, 1000);
			}
			if (msg.what == 2) {
				CommandManager.sendSetOnlineMessage();
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.center_layout);
		mBaseContext = this;
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		mScreenWidth = display.getWidth();
		mScreenHeight = display.getHeight();
		mHandler = new MessageHandler(Looper.getMainLooper());
		mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mUtil = CommonUtil.getInstance(mBaseContext);
		// mCurrentCommand = Command.COMMANDS.COMMAND_TEACH_DEMONSTRATION;
		mCoverView = (RelativeLayout) findViewById(R.id.cover_view);
		mErrorMsgView = (TextView) findViewById(R.id.error_message);
		mRadioGroup = (RadioGroup) findViewById(R.id.broadcast_group);
		mRadioGroup.setOnCheckedChangeListener(this);
		registerNetworkReceiver();
		if (!CommonUtil.isConfigIP()) {
			handle_saveconf(CommonUtil.getIpPrefix());
			if (mHandler != null) {
				mHandler.removeMessages(MessageHandler.UPDATE_NET_STATUS);
				mHandler.sendEmptyMessageDelayed(
						MessageHandler.UPDATE_NET_STATUS, 2000);
			}
			CommonUtil.configIP(true);
		} else {
			if (checkNetworkAvailable()) {
				preInit();
			} else {
				mCoverView.setVisibility(View.VISIBLE);
				mErrorMsgView.setText(R.string.network_off_alert);
				Log.e(TAG, "Internet disconnected, Init failed!");
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	private class NetworkStatusReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (checkNetworkAvailable()) {
					preInit();
					// if (sIsOnline) {
					// mCoverView.setVisibility(View.VISIBLE);
					// mErrorMsgView.setText(R.string.online_alert);
					// } else {
					// mCoverView.setVisibility(View.GONE);
					// }
				} else {
					mCoverView.setVisibility(View.VISIBLE);
					mErrorMsgView.setText(R.string.network_off_alert);
					if (mLibSiren != null) {
						mLibSiren.destroy();
					}
				}
			}
		}
	}

	/**
	 * Check the internet state and notify the state.
	 * 
	 * @return true if network available.
	 */
	public boolean checkNetworkAvailable() {
		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();

		if (networkInfo != null) {
			mIsInternetConnected = networkInfo.isConnected();
		} else {
			mIsInternetConnected = false;
		}
		Log.d(TAG, "connect status " + mIsInternetConnected);
		Toast.makeText(getApplicationContext(),
				"Network status: " + mIsInternetConnected, Toast.LENGTH_LONG)
				.show();
		return mIsInternetConnected;
	}

	private void registerNetworkReceiver() {
		if (!mNetworkReceiverRegistered) {
			// we only care about the network connect state change
			IntentFilter filter = new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(mNetworkReceiver, filter);
			mNetworkReceiverRegistered = true;
		}
	}

	private void unregisterNetworkReceiver() {
		if (mNetworkReceiverRegistered) {
			unregisterReceiver(mNetworkReceiver);
			mNetworkReceiverRegistered = false;
		}
	}

	private void preInit() {
		CommonUtil.reStoreValueIntoSharePreferences(CommonUtil.SEATNO,
				"teacher");
		mSeatNo = CommonUtil.getSeatNo();
		CommandManager.SetSeatNo(mSeatNo);
		initView();
//		mTimeHandler.sendEmptyMessageDelayed(1, 1000);
		// parseMessage(content);
		// Log.d(TAG, "LibSiren initialisation");
		try {

			mLibSiren = LibSiren.getInstance();

		} catch (LibSirenException e) {
			// Log.d(TAG, "LibSiren initialisation failed");
			return;
		}
		try {
			// ToDo:return value
			mLibSiren.init(getBaseContext());
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CommandManager.SetLibSiren(mLibSiren);
			CommandManager.sendSetOnlineMessage();
			mTimeHandler.sendEmptyMessageDelayed(2, delayTime);
			CommandManager.startSat();
			CommandManager.openMic();
			CommandManager.openSpeaker();
			CommandManager.sendSetAllCallMessage();
			mCurrentCommand = Command.COMMANDS.COMMAND_TEACH_DEMONSTRATION;
			// instance the event handler
			em.addHandler(baseEventHandler);

		} catch (LibSirenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected void onStop() {
		em.removeHandler(baseEventHandler);
		super.onStop();
	}

	private void resetStatus() {
		CommandManager.closeMic();
		CommandManager.openSpeaker();
		CommandManager.sendSetVGAMessage("off");
	}

	/**
	 * Handle libsiren asynchronous events
	 */
	private final Handler baseEventHandler = new BaseJNIEventHandler(this);

	private class BaseJNIEventHandler extends WeakHandler<ControllerActivity> {
		public BaseJNIEventHandler(ControllerActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			ControllerActivity activity = getOwner();
			if (activity == null)
				return;
			// Do not handle events if we are leaving the TestJniActivity
			// ToDo:
			Intent acionIntent = new Intent();
			String data = msg.getData().getString("data");

			// Log.d(TAG, data);

			MediaMessage message = CommonUtil.parseMessage(data);
			if (message == null) {
				return;
			}
			Log.d(TAG, "message1 " + message.toString());
			// Receive the command from teacher
			if ("cmd".equals(message.getTpye())) {
				if (message.getCommand().equals("order_set")) {
					CommonUtil.reStoreValueIntoSharePreferences(
							CommonUtil.ROW_COLUMN, message.getParams());
					String[] seatInfo = CommonUtil.getRowColumn().split(",");
					mRow = Integer.parseInt(seatInfo[0]);
					mColumn = Integer.parseInt(seatInfo[1]);
					int unitHeight = (mScreenHeight - 100) / mRow;
					int unitwidth = (mScreenWidth - 100) / mColumn;
					int columnWidth = Math.min(unitHeight, unitwidth);
					mSeatView.setColumnWidth(columnWidth);
					mSeatView.setNumColumns(mColumn);
					mStudentList.clear();
					mStudentList = sortSeat(mRow, mColumn);
					// mAdapter.notifyDataSetChanged();
					mAdapter = new StudentAdapter(getApplicationContext(),
							mStudentList);
					mSeatView.setAdapter(mAdapter);
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_TEACH_ALLCALLS)) {
					CommandManager.startSat();
					CommandManager.leaveGroup("");
					CommandManager.joinGroup("230.1.2.3");
					CommandManager.openMic();
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_GLOBAL_ONLINE)) {
					sIsOnline = true;
					mDeviceStateView.setText(R.string.online);
					// mCoverView.setVisibility(View.VISIBLE);
					mErrorMsgView.setText(R.string.online_alert);
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_GLOBAL_RESAET)) {
					resetStatus();
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_GROUP_EXIT)) {
					sIsOnline = false;
					mCoverView.setVisibility(View.GONE);
					mDeviceStateView.setText(R.string.offline);
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_SETTING)) {
					Intent intent = new Intent();
					intent.setClassName("com.android.settings",
							"com.android.settings.Settings");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}// volume
				else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_SOUND)) {
					try {
						int sound = Integer.parseInt(message.getParams());
						Log.d("sound", "sound " + sound);
						CommandManager.setSound(sound);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// Mic
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_MIC)) {
					if (message.getParams().equals("on")) {
						mMicrophoneButton
								.setBackgroundResource(R.drawable.mic_open_button);
						CommandManager.setMicFlag(true);
						CommandManager.openMic();
					} else if (message.getParams().equals("off")) {
						mMicrophoneButton
								.setBackgroundResource(R.drawable.mic_close_button);
						CommandManager.setMicFlag(false);
						CommandManager.closeMic();
					}
					// Speaker
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_SPEAKER)) {
					if (message.getParams().equals("on")) {
						CommandManager.openSpeaker();
						mSpeakerButton
								.setBackgroundResource(R.drawable.speaker_open_button);
					} else if (message.getParams().equals("off")) {
						CommandManager.closeSpeaker();
						mSpeakerButton
								.setBackgroundResource(R.drawable.speaker_close_button);
					}
					// Headset
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_HEADSET)) {
					if (message.getParams().equals("on")) {
						CommandManager.openHeadset();
						mHeadSetButton
								.setBackgroundResource(R.drawable.headset_open_button);
					} else if (message.getParams().equals("off")) {
						CommandManager.closeHeadset();
						mHeadSetButton
								.setBackgroundResource(R.drawable.headset_close_button);
					}
				}
				// line in
				else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_TAPE)) {
					if (message.getParams().equals("on")) {
						CommandManager.openLinein();
						// mSpeakerButton
						// .setBackgroundResource(R.drawable.speaker_open_button);
					} else if (message.getParams().equals("off")) {
						CommandManager.closeLinein();
						// mSpeakerButton
						// .setBackgroundResource(R.drawable.speaker_close_button);
					}
					// tone
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_TONE)) {
					try {
						int value = Integer.parseInt(message.getParams());
						CommandManager.setToneUp(value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_GROUP_ATTEND_DISCUSS)) {
					if (message.isMe()) {
						CommandManager.leaveGroup("");
						mGroupAddr = GroupInfo.getGroupId("g"
								+ message.getGroup());
						CommandManager.joinGroup(mGroupAddr);
						CommandManager.openMic();
					}

				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_GROUP_EXIT_DISCUSS)) {
					if (message.isMe()) {
						CommandManager.leaveGroup(mGroupAddr);
					}

				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_TEACH_INTERCOM)) {
					CommandManager.leaveGroup("");
					mGroupAddr = GroupInfo.getGroupId(message.getGroup());
					CommandManager.joinGroup(mGroupAddr);
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_REBOOT)) {
					if (message.isMe()) {
						Log.d(TAG, "rebot!");
						long delayTime = 20 * 60 * 1000;
						try {
							if (Integer.parseInt(message.getParams()) > 0) {
								delayTime = Integer.parseInt(message
										.getParams());
							}
						} catch (Exception e) {
							Log.d(TAG, e.toString());
						}
						mTimeHandler.postDelayed(new Runnable() {

							@Override
							public void run() {

								PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
								pManager.reboot("reboot");

							}
						}, delayTime);

					}
				}
				// Receive the command from student
			} else {
				if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_GLOBAL_ONLINE)) {
					for (Student student : mStudentList) {
						if (student.getmSearNo().equals(message.getTpye())) {
							student.setOnline(true);
						}
						Log.d(TAG, "student online " + student.getmSearNo());
						mAdapter.notifyDataSetChanged();
					}
				} else if (message.getCommand().equals(
						Command.COMMANDS.COMMAND_GLOBAL_HANDSUP)) {
					for (Student student : mStudentList) {
						if (student.getmSearNo().equals(message.getTpye())) {
							if (message.getParams().equals("on")) {
								student.setHandUp(true);
							} else {
								student.setHandUp(false);
							}
						}
						String status = message.getTpye() + " "
								+ getString(R.string.hand_up);
						mStudentStateView.setText(status);
						mAdapter.notifyDataSetChanged();
					}
				}

			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterNetworkReceiver();
		mLibSiren.destroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void initView() {
		String[] seatInfo = CommonUtil.getRowColumn().split(",");
		mRow = Integer.parseInt(seatInfo[0]);
		mColumn = Integer.parseInt(seatInfo[1]);
		// Log.d(TAG, "seat info" + mRow + "----" + mColumn);
		mStudentList = new ArrayList<Student>();
		mStudentList = sortSeat(mRow, mColumn);
		mSeatView = (GridView) findViewById(R.id.seats_layout);
		/*
		 * mCoverView = (RelativeLayout) findViewById(R.id.cover_view);
		 * mErrorMsgView = (TextView)findViewById(R.id.error_message);
		 */
		int unitHeight = (mScreenHeight - 100) / mRow;
		int unitwidth = (mScreenWidth - 100) / mColumn;
		int columnWidth = Math.min(unitHeight, unitwidth);
		mSeatView.setColumnWidth(columnWidth);
		mSeatView.setNumColumns(mColumn);
		mSeatView.setOnItemSelectedListener(this);
		mSeatView.setOnItemClickListener(this);
		mAdapter = new StudentAdapter(getApplicationContext(), mStudentList);
		mSeatView.setAdapter(mAdapter);
		mSwitchTeachButton = (LinearLayout) findViewById(R.id.teach_model);
		mSwitchTeachButton.setOnClickListener(this);
		mSwitchTeachButton.setEnabled(false);
		mSwitchGroupButton = (LinearLayout) findViewById(R.id.discuss_model);
		mSwitchGroupButton.setOnClickListener(this);
		mSwitchSelfButton = (LinearLayout) findViewById(R.id.self_study_model);
		mSwitchSelfButton.setOnClickListener(this);

		mDemonButton = (LinearLayout) findViewById(R.id.demonstrate);
		mDemonButton.setOnClickListener(this);
		mIntercomButton = (LinearLayout) findViewById(R.id.intercom);
		mIntercomButton.setOnClickListener(this);
		mMonitorButton = (LinearLayout) findViewById(R.id.monitor);
		mMonitorButton.setOnClickListener(this);
		mSetButton = (ImageButton) findViewById(R.id.setting);
		mSetButton.setOnClickListener(this);
		mVersionView = (TextView) findViewById(R.id.version);
		mVersionView.setVisibility(View.GONE);
		mSpeakerButton = (Button) findViewById(R.id.speaker);
		mSpeakerButton.setOnClickListener(this);
		mMicrophoneButton = (Button) findViewById(R.id.microphone);
		mMicrophoneButton.setOnClickListener(this);

		mHeadSetButton = (Button) findViewById(R.id.headset);
		mHeadSetButton.setOnClickListener(this);
		mDeviceStateView = (TextView) findViewById(R.id.machine_status_view);
//		mTimeView = (TextView) findViewById(R.id.time_status_view);
		mStudentStateView = (TextView) findViewById(R.id.student_status_view);
		mGroupSum2 = (LinearLayout) findViewById(R.id.group_sum_2);
		mGroupSum4 = (LinearLayout) findViewById(R.id.group_sum_4);
		mGroupSum2.setOnClickListener(this);
		mGroupSum4.setOnClickListener(this);

		// mExitBtn = (Button) findViewById(R.id.exit);
		// mExitBtn.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.teach_model:
			mDemonButton.setEnabled(true);
			mIntercomButton.setEnabled(true);
			mMonitorButton.setEnabled(true);
			mSwitchTeachButton.setEnabled(false);
			mSwitchSelfButton.setEnabled(true);
			mSwitchGroupButton.setEnabled(true);
			mGroupSum4.setVisibility(View.GONE);
			mGroupSum2.setVisibility(View.GONE);
			mDemonButton.setVisibility(View.VISIBLE);
			mIntercomButton.setVisibility(View.VISIBLE);
			mMonitorButton.setVisibility(View.VISIBLE);
			restoreStudents();
			CommandManager.sendSwitchTeachModel();
			break;
		case R.id.discuss_model:
			restoreStudents();
			mSwitchTeachButton.setEnabled(true);
			mSwitchSelfButton.setEnabled(true);
			mSwitchGroupButton.setEnabled(false);
			mGroupSum4.setVisibility(View.VISIBLE);
			mGroupSum2.setVisibility(View.VISIBLE);
			mGroupSum4.setEnabled(true);
			mGroupSum2.setEnabled(true);
			mDemonButton.setVisibility(View.GONE);
			mIntercomButton.setVisibility(View.GONE);
			mMonitorButton.setVisibility(View.GONE);
			sendDiscussAllStudents(mGroupUnit);
			break;
		case R.id.self_study_model:
			restoreStudents();
			mSwitchTeachButton.setEnabled(true);
			mSwitchSelfButton.setEnabled(false);
			mSwitchGroupButton.setEnabled(true);
			mGroupSum4.setVisibility(View.GONE);
			mGroupSum2.setVisibility(View.GONE);
			mDemonButton.setVisibility(View.GONE);
			mIntercomButton.setVisibility(View.GONE);
			mMonitorButton.setVisibility(View.GONE);
			CommandManager.sendSwitchStudyCommand();
			break;
		case R.id.demonstrate:
			mDemonButton.setEnabled(false);
			mIntercomButton.setEnabled(true);
			mMonitorButton.setEnabled(true);
			mCurrentCommand = Command.COMMANDS.COMMAND_TEACH_DEMONSTRATION;
			restoreStudents();
			CommandManager.sendSetAllCallMessage();
			mDemonButton
					.setBackgroundResource(R.drawable.book_toc_btn_press_left);
			mIntercomButton.setBackgroundResource(R.drawable.left_button);
			mMonitorButton.setBackgroundResource(R.drawable.left_button);
			mGroupSum2.setBackgroundResource(R.drawable.left_button);
			mGroupSum4.setBackgroundResource(R.drawable.left_button);
			break;
		case R.id.intercom:
			mDemonButton.setEnabled(true);
			mIntercomButton.setEnabled(false);
			mMonitorButton.setEnabled(true);
			mCurrentCommand = Command.COMMANDS.COMMAND_TEACH_INTERCOM;
			restoreStudents();
			CommandManager.sendSetAllCallMessage();
			mDemonButton.setBackgroundResource(R.drawable.left_button);
			mIntercomButton
					.setBackgroundResource(R.drawable.book_toc_btn_press_left);
			mMonitorButton.setBackgroundResource(R.drawable.left_button);

			break;
		case R.id.monitor:
			mDemonButton.setEnabled(true);
			mIntercomButton.setEnabled(true);
			mMonitorButton.setEnabled(false);
			mCurrentCommand = Command.COMMANDS.COMMAND_TEACH_MONITOR;
			restoreStudents();
			CommandManager.sendSetAllCallMessage();
			mDemonButton.setBackgroundResource(R.drawable.left_button);
			mIntercomButton.setBackgroundResource(R.drawable.left_button);
			mMonitorButton
					.setBackgroundResource(R.drawable.book_toc_btn_press_left);

			break;
		case R.id.setting:

			SetDialogFragment dialogFragment = SetDialogFragment.newInstance();
			dialogFragment.setIUpdateSeatNo(this);
			Bundle bundle = new Bundle();
			bundle.putString("row", String.valueOf(mRow));
			bundle.putString("column", String.valueOf(mColumn));
			dialogFragment.setArguments(bundle);
			/*
			 * dialogFragment.show(getFragmentManager(), "dialog");
			 */
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			// 鎸囧畾涓�涓繃娓″姩鐢�
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			// 浣滀负鍏ㄥ睆鏄剧ず,浣跨敤鈥渃ontent鈥濅綔涓篺ragment瀹瑰櫒鐨勫熀鏈鍥�,杩欏缁堟槸Activity鐨勫熀鏈鍥�
			transaction.add(android.R.id.content, dialogFragment)
					.addToBackStack(null).commit();
			// TODO ...
			break;
		case R.id.speaker:
			if (CommandManager.getSpeakerStatus() == 0) {
				Log.d(TAG, "speaker enable");
				CommandManager.closeSpeaker();
				CommandManager.sendSetSpeakerMessage("off");
				mSpeakerButton
						.setBackgroundResource(R.drawable.speaker_close_button);
			} else if (CommandManager.getSpeakerStatus() == 1) {
				Log.d(TAG, "speaker disable");
				CommandManager.openSpeaker();
				CommandManager.sendSetSpeakerMessage("on");
				mSpeakerButton
						.setBackgroundResource(R.drawable.speaker_open_button);
			}
			// TODO ...
			break;
		case R.id.headset:
			if (CommandManager.getHeadsetStatus() == 0) {
				Log.d(TAG, "headset enable");
				CommandManager.closeHeadset();
				CommandManager.sendSetHeadsetMessage("off");
				mHeadSetButton
						.setBackgroundResource(R.drawable.headset_close_button);
			} else if (CommandManager.getHeadsetStatus() == 1) {
				Log.d(TAG, "headset disable");
				CommandManager.openHeadset();
				CommandManager.sendSetHeadsetMessage("on");
				mHeadSetButton
						.setBackgroundResource(R.drawable.headset_open_button);
			}
			// TODO ...
			break;
		case R.id.microphone:
			if (CommandManager.getMicStatus() == 0) {
				Log.d(TAG, "mic enable");
				mMicrophoneButton
						.setBackgroundResource(R.drawable.mic_close_button);
				CommandManager.setMicFlag(false);
				CommandManager.closeMic();
				CommandManager.sendSetMicMessage("off");
			} else if (CommandManager.getMicStatus() == 1) {
				Log.d(TAG, "mic disable");
				mMicrophoneButton
						.setBackgroundResource(R.drawable.mic_open_button);
				CommandManager.setMicFlag(true);
				CommandManager.openMic();
				CommandManager.sendSetMicMessage("on");
			}
			// TODO ...
			break;
		case R.id.group_sum_2:
			mGroupUnit = 2;
			mGroupSum2.setEnabled(false);
			mGroupSum4.setEnabled(true);
			sendDiscussAllStudents(mGroupUnit);

			mGroupSum2
					.setBackgroundResource(R.drawable.book_toc_btn_press_left);
			mGroupSum4.setBackgroundResource(R.drawable.left_button);

			break;
		case R.id.group_sum_4:
			mGroupUnit = 4;
			mGroupSum4.setEnabled(false);
			mGroupSum2.setEnabled(true);
			sendDiscussAllStudents(mGroupUnit);

			mGroupSum2.setBackgroundResource(R.drawable.left_button);
			mGroupSum4
					.setBackgroundResource(R.drawable.book_toc_btn_press_left);

			break;
		// case R.id.exit:
		// // finish();
		// android.os.Process.killProcess(android.os.Process.myPid());
		// break;

		}
	}

	private static Map<Integer, String> groupStudents(int m) {
		Map<Integer, String> memberMp = new HashMap<Integer, String>();
		int left = mStudentList.size() % m;
		int group = left == 0 ? mStudentList.size() / m : mStudentList.size()
				/ m + 1;
		System.out.print("group" + group + "\n");
		for (int n = 1; n <= group; n++) {
			if (n == group && left != 0) {
				StringBuilder sb = new StringBuilder();
				// sb.append(n).append(":");
				for (int i = 0; i < left; i++) {
					mStudentList.get((n - 1) * m + i).setGroupId(
							String.valueOf(n));
					sb.append(mStudentList.get((n - 1) * m + i).getmSearNo())
							.append(",");
				}
				memberMp.put(n, sb.toString());
				System.out.print("members : " + sb.toString() + "\n");
				break;
			}
			StringBuilder sb = new StringBuilder();
			// sb.append(n).append(":");
			for (int i = 0; i < m; i++) {
				mStudentList.get((n - 1) * m + i).setGroupId(String.valueOf(n));
				sb.append(mStudentList.get((n - 1) * m + i).getmSearNo())
						.append(",");
			}
			memberMp.put(n, sb.toString());
			System.out.print("members : " + sb.toString() + "\n");
		}
		mAdapter.notifyDataSetChanged();
		return memberMp;
	}

	private void sendDiscussAllStudents(int m) {
		Map<Integer, String> groupMap = groupStudents(m);
		for (Integer key : groupMap.keySet()) {
			String params = groupMap.get(key);
			String[] members = params.split(",");
			for (int i = 0; i < members.length; i++) {
				CommandManager.sendDiscussCommand(members[i], key, params);
			}
		}
	}

	private static List<Student> sortSeat(int row, int column) {
		List<Student> students = new ArrayList<Student>(row * column);
		for (int i = row, m = 0; i >= 1; m++, i--) {

			for (char j = 'A'; j < 'A' + column; j++) {
				Student s = new Student();
				s.setmSearNo(j + "" + i);
				students.add(s);
			}
			System.out.print(" \n ");
		}
		return students;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		sStudent = mStudentList.get(arg2);

		if (mCurrentCommand == null) {
			return;
		}
		if (mCurrentCommand
				.equals(Command.COMMANDS.COMMAND_TEACH_DEMONSTRATION)) {
			boolean flag = sStudent.isDemonStration();
			// is demonstration
			if (flag) {
				sStudent.setDemonStration(false);
				CommandManager.sendCleanCommand(sStudent.getmSearNo());
			} else {
				if (getCountDemonstration() >= 4) {
					Toast.makeText(mBaseContext, R.string.demonstration_enough,
							Toast.LENGTH_LONG).show();
					return;
				}
				sStudent.setDemonStration(true);
				CommandManager.sendDemonstrationCommand(sStudent.getmSearNo());
			}

			mAdapter.notifyDataSetChanged();
		} else if (mCurrentCommand
				.equals(Command.COMMANDS.COMMAND_TEACH_INTERCOM)) {
			mGroupAddr = GroupInfo.getGroupId("g1");
			CommandManager.sendIntercomCommand(sStudent.getmSearNo());
			CommandManager.leaveGroup("");
			CommandManager.joinGroup(mGroupAddr);

			boolean flag = sStudent.isInterCom();
			if (!flag) {
				sStudent.setInterCom(!flag);
			} else {
				for (int i = 0; i < mStudentList.size(); i++) {
					if (mStudentList.get(i).isInterCom()) {
						// Nothing to do..
					} else {
						mStudentList.get(i).setInterCom(false);
					}
				}
			}
			mAdapter.notifyDataSetChanged();
		} else if (mCurrentCommand
				.equals(Command.COMMANDS.COMMAND_TEACH_MONITOR)) {
			boolean flag = sStudent.isMonitored();
			if (!flag) {
				sStudent.setMonitored(!flag);
			} else {
				for (int i = 0; i < mStudentList.size(); i++) {
					if (mStudentList.get(i).isMonitored()) {
						// Nothing to do..
					} else {
						mStudentList.get(i).setMonitored(false);
					}
				}
			}
			mAdapter.notifyDataSetChanged();
			CommandManager.sendMonitorCommand(sStudent.getmSearNo());
		}

		Log.d(TAG, "select item " + sStudent.toString() + " current command "
				+ mCurrentCommand);

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		sStudent = mStudentList.get(arg2);

		if (mCurrentCommand == null) {
			return;
		}
		if (mCurrentCommand
				.equals(Command.COMMANDS.COMMAND_TEACH_DEMONSTRATION)) {
			boolean flag = sStudent.isDemonStration();
			// is demonstration
			if (flag) {
				sStudent.setDemonStration(false);
				CommandManager.sendCleanCommand(sStudent.getmSearNo());
			} else {
				if (getCountDemonstration() > 3) {
					Toast.makeText(mBaseContext, R.string.demonstration_enough,
							Toast.LENGTH_LONG).show();
					return;
				}
				sStudent.setDemonStration(true);
				CommandManager.sendDemonstrationCommand(sStudent.getmSearNo());
			}

			mAdapter.notifyDataSetChanged();
		} else if (mCurrentCommand
				.equals(Command.COMMANDS.COMMAND_TEACH_INTERCOM)) {
			mGroupAddr = GroupInfo.getGroupId("g1");
			CommandManager.sendIntercomCommand(sStudent.getmSearNo());
			CommandManager.leaveGroup("");
			CommandManager.joinGroup(mGroupAddr);

			boolean flag = sStudent.isInterCom();
			restoreStudents();
			if (!flag) {
				sStudent.setInterCom(!flag);
			}
			mAdapter.notifyDataSetChanged();
		} else if (mCurrentCommand
				.equals(Command.COMMANDS.COMMAND_TEACH_MONITOR)) {
			CommandManager.sendMonitorCommand(sStudent.getmSearNo());
		}

		Log.d(TAG, "click item " + sStudent.toString() + " current command "
				+ mCurrentCommand);

	}

	static class StudentAdapter extends BaseAdapter {
		private Context mContext;
		private List<Student> mStudents;

		public StudentAdapter(Context context, List<Student> students) {
			this.mContext = context;
			this.mStudents = students;
		}

		@Override
		public int getCount() {

			return this.mStudents.size();
		}

		@Override
		public Object getItem(int position) {
			return this.mStudents.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View contentView, ViewGroup viewGroup) {

			contentView = LayoutInflater.from(mContext).inflate(
					R.layout.grid_item, null);
			TextView nameView = (TextView) contentView
					.findViewById(R.id.nameView);
			ImageView stateView = (ImageView) contentView
					.findViewById(R.id.statusView);
			TextView groupView = (TextView) contentView
					.findViewById(R.id.groupView);
			Student s = mStudents.get(position);
			if (s.isOnline()) {
				stateView.setImageResource(R.drawable.online);
				nameView.setTextColor(Color.BLACK);
				if (Command.COMMANDS.COMMAND_TEACH_DEMONSTRATION
						.equals(mCurrentCommand)) {
					if (s.isDemonStration()) {
						stateView
								.setImageResource(R.drawable.demonstration_sel);
					}
				} else if (Command.COMMANDS.COMMAND_TEACH_INTERCOM
						.equals(mCurrentCommand)) {
					if (s.isInterCom()) {
						stateView.setImageResource(R.drawable.intercom_sel);
					}
				} else if (Command.COMMANDS.COMMAND_TEACH_MONITOR
						.equals(mCurrentCommand)) {
					if (s.isMonitored()) {
						stateView.setImageResource(R.drawable.monitor_sel);
					}
				} else if (Command.COMMANDS.COMMAND_GLOBAL_HANDSUP
						.equals(mCurrentCommand)) {
					if (s.isHandUp()) {
						stateView.setImageResource(R.drawable.handup_sel);
					}
				}
			} else {
				nameView.setTextColor(Color.DKGRAY);
				stateView.setImageResource(R.drawable.offline);
			}

			if (TextUtils.isEmpty(s.getGroupId())) {
				groupView.setVisibility(View.GONE);
			} else {
				groupView.setText(s.getGroupId());
				groupView.setVisibility(View.VISIBLE);
			}
			nameView.setText(s.getmSearNo());
			return contentView;
		}
	}

	@Override
	public void update(int row, int column) {
		mRow = row;
		mColumn = column;
		mStudentList.clear();
		mStudentList = sortSeat(mRow, mColumn);
		mSeatView.setNumColumns(mColumn);
		// Log.d(TAG, "seat info" + mRow + "----" + mColumn);
		// mAdapter.notifyDataSetChanged();
		mAdapter = new StudentAdapter(getApplicationContext(), mStudentList);
		mSeatView.setAdapter(mAdapter);
	}

	private void restoreStudents() {
		for (int i = 0; i < mStudentList.size(); i++) {
			Student st = mStudentList.get(i);
			st.setHandUp(false);
			st.setGroupId(null);
			st.setDemonStration(false);
			st.setInterCom(false);
		}
		mAdapter.notifyDataSetChanged();
	}

	private int getCountDemonstration() {
		int count = 0;
		for (int i = 0; i < mStudentList.size(); i++) {
			if (mStudentList.get(i).isDemonStration()) {
				count++;
				Log.d(TAG, mStudentList.get(i).getmSearNo() + " count " + count);
			}
		}
		return count;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int action = event.getAction();
		if (keyCode == KeyEvent.KEYCODE_F9) {
			if (action == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
				Intent intent = new Intent();
				intent.setClassName("com.android.settings",
						"com.android.settings.Settings");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_F8) {
			if (mVersionView.getVisibility() == View.GONE
					|| mVersionView.getVisibility() == View.INVISIBLE) {
				mVersionView.setVisibility(View.VISIBLE);
			} else {
				mVersionView.setVisibility(View.GONE);
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void handle_saveconf(String mIpPrefix) {
		// Log.d("ethernet info", "IP :" + mEthInfo.getIpAddress() + " "
		// + mEthInfo.getConnectMode());
		// EthernetDevInfo info = new EthernetDevInfo();
		// info.setIfName(mEthInfo.getIfName());
		//
		// info.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_MANUAL);
		// info.setIpAddress(mIpPrefix + ".15" );
		// info.setRouteAddr(mIpPrefix + ".1");
		// info.setDnsAddr(mIpPrefix + ".1");
		// info.setNetMask("255.255.255.0");
		// mEthManager.updateEthDevInfo(info);
	}

	private class MessageHandler extends Handler {
		public static final int UPDATE_NET_STATUS = 0;
		public static final int UPDATE_EXIT_STATUS = 1;

		MessageHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_NET_STATUS:
				if (checkNetworkAvailable()) {
					if (mLibSiren != null) {
						mLibSiren.destroy();
					}
					synchronized (sObjectLock) {
						new Thread(new Runnable() {

							@Override
							public void run() {
								preInit();
							}
						}).start();
					}
				} else {
					return;
				}
				break;

			case UPDATE_EXIT_STATUS:
				finish();
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ControllerActivity.this.finish();
			}

		};
		CommonUtil.showWarnDialog(ControllerActivity.this, getResources()
				.getString(R.string.exit_title),
				getResources().getString(R.string.exit_message), exitListener);
		// super.onBackPressed();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.check_local:
			CommandManager.sendBroadcastMessage("off");
			break;
		case R.id.check_remote:
			CommandManager.sendBroadcastMessage("on");
			break;
		default:
			break;
		}

	}

}
