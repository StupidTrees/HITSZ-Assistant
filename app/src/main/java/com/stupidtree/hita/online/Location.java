package com.stupidtree.hita.online;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.stupidtree.hita.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;

public class Location extends BmobObject  implements Comparable{
    String name;
    String imageURL;
    String type;
    private double latitude;
    private double longitude;
    String PositionIntroduction;
    String Record;
    String address;
    int studentnum;
    float rate;
    int search;
    String infos;

    public Location(String name, String imageURL, String type, double latitude, double longitude, String positionIntroduction, String record, String address, int studentnum, float rate, String infos) {
        this.name = name;
        this.imageURL = imageURL;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        PositionIntroduction = positionIntroduction;
        Record = record;
        this.address = address;
        this.studentnum = studentnum;
        this.rate = rate;
        this.infos = infos;

    }

    public Location(){
    }
    public Location(Location l){
        name = l.getName();
        imageURL = l.getImageURL();
        type = l.getType();
        latitude = l.getLatitude();
        longitude = l.getLongitude();
        PositionIntroduction = l.getPositionIntroduction();
        Record = l.getRecord();
        address = l.getAddress();
        studentnum = l.getStudentnum();
        rate = l.getRate();
        infos = l.getInfos();
        setObjectId(l.getObjectId());
        this.search = l.getSearch();
    }

    public int getSearch() {
        return search;
    }

    public void setSearch(int search) {
        this.search = search;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public int getStudentnum() {
        return studentnum;
    }

    public void setStudentnum(int studentnum) {
        this.studentnum = studentnum;
    }


    public ArrayList<HashMap> getInfoListArray() {
        return new ArrayList<>();
    }

    public JsonObject getInfoJsonObject() {
        return new JsonObject();
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    @Override
    public String toString() {
        return name+","+PositionIntroduction;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getPositionIntroduction() {
        return PositionIntroduction;
    }

    public void setPositionIntroduction(String positionIntroduction) {
        PositionIntroduction = positionIntroduction;
    }

    public String getRecord() {
        return Record;
    }

    public void setRecord(String record) {
        Record = record;
    }

    public String getInfos() {
        return infos;
    }

    public void setInfos(String infos) {
        this.infos = infos;
    }

    public String getType() {
        return type;
    }
    public String getType_Name() {
        if(type.equals("canteen")) return "食堂";
        if(type.equals("classroom")) return "教室";
        if(type.equals("scenery")) return "景点";
        if(type.equals("facility")) return "设施";
        if(type.equals("dormitory")) return "宿舍";
        return "地点";
    }
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int compareTo(Object o) {
        if(rate<((Location)o).rate){
            return 1;
        }else if(rate>((Location)o).rate) return -1;
        else return 0;
    }

    public static void showRateDialog(final Context context, final Location t, final SaveListener successListener) {
        if (CurrentUser == null) {
            Toast.makeText(HContext, "请先登录!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(t==null) return;
        BmobQuery<RateUser> bmobQuery = new BmobQuery();
        bmobQuery.addWhereEqualTo("hitaUser", CurrentUser);
        bmobQuery.addWhereEqualTo("rateObjectId", t.getObjectId());
        bmobQuery.findObjects(new FindListener<RateUser>() {
            @Override
            public void done(List<RateUser> list, BmobException e) {
                if (e != null) {
                    Log.e("!!", e.toString());
                    return;
                }
                if (list != null && list.size() > 0) {
                    Toast.makeText(HContext, "您已提交过评分，请下个评分周期再来进行评价", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog ad = new AlertDialog.Builder(context).create();
                    View v = LayoutInflater.from(context).inflate(R.layout.dialog_rate_canteen, null);

                    final RatingBar rb = v.findViewById(R.id.canteen_rating);
                    final float rateOld = t.getRate();
                    final int stuNumOld = t.getStudentnum();
                    rb.setNumStars(5);
                    rb.setStepSize(0.5f);
                    rb.setRating(10.0f);
                    ad.setTitle("为"+t.getName()+"评分");
                    ad.setView(v);
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "提交", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            t.setRate((rateOld * stuNumOld + rb.getRating() * 2) / (stuNumOld + 1));
                            t.setStudentnum(stuNumOld + 1);
                            Location x = new Location(t);
                            x .update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    RateUser u = new RateUser();
                                    u.setHitaUser(CurrentUser);
                                    u.setRateObjectId(t.getObjectId());
                                    u.save(successListener);
                                }
                            });

                        }
                    });
                    ad.show();
                }
            }
        });

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(name, location.name) &&
                Objects.equals(type, location.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    public static int getListLayout(){return -1;}
}
