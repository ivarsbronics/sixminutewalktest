package com.ivarsbronics.sixminutewalktest;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class TestInfo implements Parcelable {
    private String
            testTimeInMillis,
            testDateTime,
            gender,
            age,
            height,
            weight,
            prepPhaseHRStart,
            prepPhaseHREnd,
            prepPhaseHRMin,
            prepPhaseHRMax,
            preTestValueDyspnea,
            preTestValueFatigue,
            preTestBloodPressureSystolic,
            preTestBloodPressureDiastolic,
            preTestOxygenSaturation,
            postTestValueDyspnea,
            postTestValueFatigue,
            postTestBloodPressureSystolic,
            postTestBloodPressureDiastolic,
            postTestOxygenSaturation,
            totalDistance,
            userTotalDistance,
            deviceName,
            hrMaxByFormula,
            testAverageHR,
            testMaxHR,
            testMinHR,
            hrBelowZone1Percent,
            hrZone1Percent,
            hrZone2Percent,
            hrZone3Percent,
            hrZone4Percent,
            hrZone5Percent,
            hrAboveZone5Percent,
            endTestPrematurely,
            estimatedDistance,
            hrMonitorSkipped
                    ;
    private HashMap<String, String> hrMap = new HashMap();
    private HashMap<String, LatLng> locationMap = new HashMap();

    public TestInfo() {
    }

    protected TestInfo(Parcel in) {
        testTimeInMillis = in.readString();
        testDateTime = in.readString();
        gender = in.readString();
        age = in.readString();
        height = in.readString();
        weight = in.readString();
        prepPhaseHRStart = in.readString();
        prepPhaseHREnd = in.readString();
        prepPhaseHRMin = in.readString();
        prepPhaseHRMax = in.readString();
        preTestValueDyspnea = in.readString();
        preTestValueFatigue = in.readString();
        preTestBloodPressureSystolic = in.readString();
        preTestBloodPressureDiastolic = in.readString();
        preTestOxygenSaturation = in.readString();
        postTestValueDyspnea = in.readString();
        postTestValueFatigue = in.readString();
        postTestBloodPressureSystolic = in.readString();
        postTestBloodPressureDiastolic = in.readString();
        postTestOxygenSaturation = in.readString();
        totalDistance = in.readString();
        userTotalDistance = in.readString();
        deviceName = in.readString();
        hrMaxByFormula = in.readString();
        testAverageHR = in.readString();
        testMaxHR = in.readString();
        testMinHR = in.readString();
        hrBelowZone1Percent = in.readString();
        hrZone1Percent = in.readString();
        hrZone2Percent = in.readString();
        hrZone3Percent = in.readString();
        hrZone4Percent = in.readString();
        hrZone5Percent = in.readString();
        hrAboveZone5Percent = in.readString();
        endTestPrematurely = in.readString();
        estimatedDistance = in.readString();
        hrMonitorSkipped = in.readString();
    }

    public static final Creator<TestInfo> CREATOR = new Creator<TestInfo>() {
        @Override
        public TestInfo createFromParcel(Parcel in) {
            return new TestInfo(in);
        }

        @Override
        public TestInfo[] newArray(int size) {
            return new TestInfo[size];
        }
    };

    public String getTestTimeInMillis() {
        return testTimeInMillis;
    }

    public void setTestTimeInMillis(String testTimeInMillis) {
        this.testTimeInMillis = testTimeInMillis;
    }

    public String getTestDateTime() {
        return testDateTime;
    }

    public void setTestDateTime(String testDateTime) {
        this.testDateTime = testDateTime;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPrepPhaseHRStart() {
        return prepPhaseHRStart;
    }

    public void setPrepPhaseHRStart(String prepPhaseHRStart) {
        this.prepPhaseHRStart = prepPhaseHRStart;
    }

    public String getPrepPhaseHREnd() {
        return prepPhaseHREnd;
    }

    public void setPrepPhaseHREnd(String prepPhaseHREnd) {
        this.prepPhaseHREnd = prepPhaseHREnd;
    }

    public String getPrepPhaseHRMin() {
        return prepPhaseHRMin;
    }

    public void setPrepPhaseHRMin(String prepPhaseHRMin) {
        this.prepPhaseHRMin = prepPhaseHRMin;
    }

    public String getPrepPhaseHRMax() {
        return prepPhaseHRMax;
    }

    public void setPrepPhaseHRMax(String prepPhaseHRMax) {
        this.prepPhaseHRMax = prepPhaseHRMax;
    }

    public String getPreTestValueDyspnea() {
        return preTestValueDyspnea;
    }

    public void setPreTestValueDyspnea(String preTestValueDyspnea) {
        this.preTestValueDyspnea = preTestValueDyspnea;
    }

    public String getPreTestValueFatigue() {
        return preTestValueFatigue;
    }

    public void setPreTestValueFatigue(String preTestValueFatigue) {
        this.preTestValueFatigue = preTestValueFatigue;
    }

    public String getPreTestBloodPressureSystolic() {
        return preTestBloodPressureSystolic;
    }

    public void setPreTestBloodPressureSystolic(String preTestBloodPressureSystolic) {
        this.preTestBloodPressureSystolic = preTestBloodPressureSystolic;
    }

    public String getPreTestBloodPressureDiastolic() {
        return preTestBloodPressureDiastolic;
    }

    public void setPreTestBloodPressureDiastolic(String preTestBloodPressureDiastolic) {
        this.preTestBloodPressureDiastolic = preTestBloodPressureDiastolic;
    }

    public String getPreTestOxygenSaturation() {
        return preTestOxygenSaturation;
    }

    public void setPreTestOxygenSaturation(String preTestOxygenSaturation) {
        this.preTestOxygenSaturation = preTestOxygenSaturation;
    }

    public String getPostTestValueDyspnea() {
        return postTestValueDyspnea;
    }

    public void setPostTestValueDyspnea(String postTestValueDyspnea) {
        this.postTestValueDyspnea = postTestValueDyspnea;
    }

    public String getPostTestValueFatigue() {
        return postTestValueFatigue;
    }

    public void setPostTestValueFatigue(String postTestValueFatigue) {
        this.postTestValueFatigue = postTestValueFatigue;
    }

    public String getPostTestBloodPressureSystolic() {
        return postTestBloodPressureSystolic;
    }

    public void setPostTestBloodPressureSystolic(String postTestBloodPressureSystolic) {
        this.postTestBloodPressureSystolic = postTestBloodPressureSystolic;
    }

    public String getPostTestBloodPressureDiastolic() {
        return postTestBloodPressureDiastolic;
    }

    public void setPostTestBloodPressureDiastolic(String postTestBloodPressureDiastolic) {
        this.postTestBloodPressureDiastolic = postTestBloodPressureDiastolic;
    }

    public String getPostTestOxygenSaturation() {
        return postTestOxygenSaturation;
    }

    public void setPostTestOxygenSaturation(String postTestOxygenSaturation) {
        this.postTestOxygenSaturation = postTestOxygenSaturation;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public String getUserTotalDistance() {
        return userTotalDistance;
    }

    public void setUserTotalDistance(String userTotalDistance) {
        this.userTotalDistance = userTotalDistance;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getHrMaxByFormula() {
        return hrMaxByFormula;
    }

    public void setHrMaxByFormula(String hrMaxByFormula) {
        this.hrMaxByFormula = hrMaxByFormula;
    }

    public String getTestAverageHR() {
        return testAverageHR;
    }

    public void setTestAverageHR(String testAverageHR) {
        this.testAverageHR = testAverageHR;
    }

    public String getTestMaxHR() {
        return testMaxHR;
    }

    public void setTestMaxHR(String testMaxHR) {
        this.testMaxHR = testMaxHR;
    }

    public String getTestMinHR() {
        return testMinHR;
    }

    public void setTestMinHR(String testMinHR) {
        this.testMinHR = testMinHR;
    }

    public String getHrBelowZone1Percent() {
        return hrBelowZone1Percent;
    }

    public void setHrBelowZone1Percent(String hrBelowZone1Percent) {
        this.hrBelowZone1Percent = hrBelowZone1Percent;
    }

    public String getHrZone1Percent() {
        return hrZone1Percent;
    }

    public void setHrZone1Percent(String hrZone1Percent) {
        this.hrZone1Percent = hrZone1Percent;
    }

    public String getHrZone2Percent() {
        return hrZone2Percent;
    }

    public void setHrZone2Percent(String hrZone2Percent) {
        this.hrZone2Percent = hrZone2Percent;
    }

    public String getHrZone3Percent() {
        return hrZone3Percent;
    }

    public void setHrZone3Percent(String hrZone3Percent) {
        this.hrZone3Percent = hrZone3Percent;
    }

    public String getHrZone4Percent() {
        return hrZone4Percent;
    }

    public void setHrZone4Percent(String hrZone4Percent) {
        this.hrZone4Percent = hrZone4Percent;
    }

    public String getHrZone5Percent() {
        return hrZone5Percent;
    }

    public void setHrZone5Percent(String hrZone5Percent) {
        this.hrZone5Percent = hrZone5Percent;
    }

    public String getHrAboveZone5Percent() {
        return hrAboveZone5Percent;
    }

    public void setHrAboveZone5Percent(String hrAboveZone5Percent) {
        this.hrAboveZone5Percent = hrAboveZone5Percent;
    }

    public HashMap<String, String> getHrMap() {
        return hrMap;
    }

    public void setHrMap(HashMap<String, String> hrMap) {
        this.hrMap = hrMap;
    }

    public HashMap<String, LatLng> getLocationMap() {
        return locationMap;
    }

    public void setLocationMap(HashMap<String, LatLng> locationMap) {
        this.locationMap = locationMap;
    }

    public String getEndTestPrematurely() {
        return endTestPrematurely;
    }

    public void setEndTestPrematurely(String endTestPrematurely) {
        this.endTestPrematurely = endTestPrematurely;
    }

    public String getEstimatedDistance() {
        return estimatedDistance;
    }

    public void setEstimatedDistance(String estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }

    public String getHrMonitorSkipped() {
        return hrMonitorSkipped;
    }

    public void setHrMonitorSkipped(String hrMonitorSkipped) {
        this.hrMonitorSkipped = hrMonitorSkipped;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(testTimeInMillis);
        parcel.writeString(testDateTime);
        parcel.writeString(gender);
        parcel.writeString(age);
        parcel.writeString(height);
        parcel.writeString(weight);
        parcel.writeString(prepPhaseHRStart);
        parcel.writeString(prepPhaseHREnd);
        parcel.writeString(prepPhaseHRMin);
        parcel.writeString(prepPhaseHRMax);
        parcel.writeString(preTestValueDyspnea);
        parcel.writeString(preTestValueFatigue);
        parcel.writeString(preTestBloodPressureSystolic);
        parcel.writeString(preTestBloodPressureDiastolic);
        parcel.writeString(preTestOxygenSaturation);
        parcel.writeString(postTestValueDyspnea);
        parcel.writeString(postTestValueFatigue);
        parcel.writeString(postTestBloodPressureSystolic);
        parcel.writeString(postTestBloodPressureDiastolic);
        parcel.writeString(postTestOxygenSaturation);
        parcel.writeString(totalDistance);
        parcel.writeString(userTotalDistance);
        parcel.writeString(deviceName);
        parcel.writeString(hrMaxByFormula);
        parcel.writeString(testAverageHR);
        parcel.writeString(testMaxHR);
        parcel.writeString(testMinHR);
        parcel.writeString(hrBelowZone1Percent);
        parcel.writeString(hrZone1Percent);
        parcel.writeString(hrZone2Percent);
        parcel.writeString(hrZone3Percent);
        parcel.writeString(hrZone4Percent);
        parcel.writeString(hrZone5Percent);
        parcel.writeString(hrAboveZone5Percent);
        parcel.writeString(endTestPrematurely);
        parcel.writeString(estimatedDistance);
        parcel.writeString(hrMonitorSkipped);
    }
}
