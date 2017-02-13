
package com.multimedia.centercontroller;

public class Student {
    private String mName;
    private String mId;
    private String mSearNo;
    private String mGroupId;
    private boolean mIsOnline;
    private boolean mIsHandUp;
    private boolean isDemonStration;
    private boolean isInterCom;
    private boolean isMonitored;

    public Student(String mName, String mId, String mSearNo, boolean mIsOnline) {
        this.mName = mName;
        this.mId = mId;
        this.mSearNo = mSearNo;
        this.mIsOnline = mIsOnline;
    }
    public Student() {
    }

    public boolean isDemonStration() {
		return isDemonStration;
	}
	public void setDemonStration(boolean isDemonStration) {
		this.isDemonStration = isDemonStration;
	}
	public boolean isInterCom() {
		return isInterCom;
	}
	public void setInterCom(boolean isInterCom) {
		this.isInterCom = isInterCom;
	}
	public boolean isHandUp() {
        return mIsHandUp;
    }
    public void setHandUp(boolean isHandUp) {
        this.mIsHandUp = isHandUp;
    }
    public String getGroupId() {
        return mGroupId;
    }
    public void setGroupId(String mGroupId) {
        this.mGroupId = mGroupId;
    }
    public String getmName() {
        return mName;
    }
    public void setmName(String mName) {
        this.mName = mName;
    }
    public String getmId() {
        return mId;
    }
    public void setmId(String mId) {
        this.mId = mId;
    }
    public String getmSearNo() {
        return mSearNo;
    }
    public void setmSearNo(String mSearNo) {
        this.mSearNo = mSearNo;
    }
    public boolean isOnline() {
        return mIsOnline;
    }
    public void setOnline(boolean issOnline) {
        this.mIsOnline = issOnline;
    }
    
    
    public boolean isMonitored() {
		return isMonitored;
	}
	public void setMonitored(boolean isMonitored) {
		this.isMonitored = isMonitored;
	}
	@Override
    public String toString() {
        return "Student [mName=" + mName + ", mId=" + mId + ", mSearNo=" + mSearNo + ", mIsOnline="
                + mIsOnline + "]";
    }

 
}
