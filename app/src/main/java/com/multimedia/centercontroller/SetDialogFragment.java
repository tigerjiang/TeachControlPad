
package com.multimedia.centercontroller;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;


public class SetDialogFragment extends DialogFragment implements OnClickListener {

    private Button mPositiveBtn, mNegativeBtn;
    private Button mLocalSetBtn, mNetSetBtn;
    private Spinner mRowSpinner, mColumnSpinner;
    private SeekBar mVolumnSeekBar;
    private EditText mAddrView;
    private Button mNotifyForNetChangeBtn;
    private RelativeLayout mLocalSetLayout, mNetSetLayout;
    private AudioManager mAudioManager;
    private TextView mVolumnView;
    private String mSeatRow = "1", mSeatColumn = "1";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sss, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSeatRow = args.getString("row");
        mSeatColumn = args.getString("column");
        setStyle(R.style.CustomDialog, R.style.CustomDialog);
    }

    static SetDialogFragment newInstance() {
        SetDialogFragment fragment = new SetDialogFragment();
        return fragment;
    }

    /**
     * The system calls this only when creating the layout in a dia//Log.
     * 当创建Dialog的时候系统将会调用
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /**
         * 你唯一可能会覆盖这个方法的原因就是当使用onCreateView()去修改任意Dialog特点的时候。例如，
         * dialog都有一个默认的标题，但是使用者可能不需要它。因此你可以去掉标题，但是你必须调用父类去获得Dialog。
         */
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void initView(View view) {
        mLocalSetLayout = (RelativeLayout) view.findViewById(R.id.local_set_layout);
        mNetSetLayout = (RelativeLayout) view.findViewById(R.id.net_set_layout);
        mPositiveBtn = (Button) view.findViewById(R.id.ok);
        mPositiveBtn.setOnClickListener(this);
        mNegativeBtn = (Button) view.findViewById(R.id.cancel);
        mNegativeBtn.setOnClickListener(this);
        mLocalSetBtn = (Button) view.findViewById(R.id.local_set);
        mLocalSetBtn.setOnClickListener(this);
        mNetSetBtn = (Button) view.findViewById(R.id.net_set);
        mNetSetBtn.setOnClickListener(this);
        mVolumnView = (TextView) view.findViewById(R.id.volumn_label);
        mRowSpinner = (Spinner) view.findViewById(R.id.seat_row);
        mColumnSpinner = (Spinner) view.findViewById(R.id.seat_column);
        mVolumnSeekBar = (SeekBar) view.findViewById(R.id.progress_bar);
        mAudioManager = (AudioManager) this.getActivity().getSystemService(Context.AUDIO_SERVICE);// 获取音量服务
        int MaxSound = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 获取系统音量最大值
        mVolumnSeekBar.setMax(MaxSound);// 音量控制Bar的最大值设置为系统音量最大值
        int currentSount = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 获取当前音量
/*        mVolumnView.setText(this.getString(R.string.volumn_label) + currentSount);
*/        mVolumnSeekBar.setProgress(currentSount);// 音量控制Bar的当前值设置为系统音量当前值
        mVolumnSeekBar.setOnSeekBarChangeListener(new SeekBarListener());

        mAddrView = (EditText) view.findViewById(R.id.set_addr_view);
        mAddrView.setText(CommonUtil.getLocalIpAddress());
        mNotifyForNetChangeBtn = (Button) view.findViewById(R.id.notify_net);
        mNotifyForNetChangeBtn.setOnClickListener(this);

        mRowSpinner.setAdapter(new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_item, CommonUtil.sSeatRow));
        mColumnSpinner.setAdapter(new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_spinner_item, CommonUtil.sSeatColumn));

        mRowSpinner.setSelection(Integer.parseInt(mSeatRow) - 1);
        mColumnSpinner.setSelection(Integer.parseInt(mSeatColumn) - 1);
        mRowSpinner
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                            int arg2,
                            long arg3) {
                        mSeatRow = CommonUtil.sSeatRow[arg2];

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO nothing to do.

                    }
                });
        mColumnSpinner
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                            int arg2,
                            long arg3) {
                        mSeatColumn = CommonUtil.sSeatColumn[arg2];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO nothing to do.

                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok:
                CommonUtil.reStoreValueIntoSharePreferences(CommonUtil.ROW_COLUMN, mSeatRow
                        + "," + mSeatColumn);
                if(mIUpdateSeatNo!=null){
                    mIUpdateSeatNo.update(Integer.parseInt(mSeatRow), Integer.parseInt(mSeatColumn));
                }
                dismiss();
                break;
            case R.id.cancel:
                dismiss();
                break;
            case R.id.local_set:
                mLocalSetBtn.setEnabled(false);
                mLocalSetLayout.setVisibility(View.VISIBLE);
                mLocalSetBtn.setBackgroundResource(R.drawable.setting_local_selected); 
                
                mNetSetBtn.setEnabled(true);
                mNetSetLayout.setVisibility(View.GONE);
                mNetSetBtn.setBackgroundResource(R.drawable.setting_net_normal); 
                break;
            case R.id.net_set:
                mLocalSetBtn.setEnabled(true);
                mLocalSetLayout.setVisibility(View.GONE);
                mLocalSetBtn.setBackgroundResource(R.drawable.setting_local_normal); 
                mNetSetBtn.setEnabled(false);
                mNetSetLayout.setVisibility(View.VISIBLE);
                mNetSetBtn.setBackgroundResource(R.drawable.setting_net_selected); 
                break;
            case R.id.notify_net:
                CommandManager.sendSetOnlineMessage();
                break;

            default:
                break;
        }

    }

    // 音量进度条
    class SeekBarListener implements OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            // TODO Auto-generated method stub
            if (fromUser) {
                int SeekPosition = seekBar.getProgress();
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, SeekPosition, 0);
            }
/*            mVolumnView.setText(SetDialogFragment.this.getString(R.string.volumn_label) + progress);
*/        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

    }

    IUpdateSeatNo mIUpdateSeatNo;

   public interface IUpdateSeatNo {
        void update(int row, int column);
    }

    public void setIUpdateSeatNo(IUpdateSeatNo mIUpdateSeatNo) {
        this.mIUpdateSeatNo = mIUpdateSeatNo;
    }

}
