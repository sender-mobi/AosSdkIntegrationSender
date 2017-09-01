package mobi.sender.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by vp
 * on 08.07.16.
 */
public class Country implements Parcelable {

    private String name, prefix, code;

    public Country(JSONObject jo) {
        name = jo.optString("name");
        code = jo.optString("country");
        prefix = jo.optString("prefix");
    }

    public Country(String name, String prefix, String code) {
        this.name = name;
        this.prefix = prefix;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return "+" + prefix;
    }

    public String getCode() {
        return code.toLowerCase();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.prefix);
        dest.writeString(this.code);
    }

    protected Country(Parcel in) {
        this.name = in.readString();
        this.prefix = in.readString();
        this.code = in.readString();
    }

    public static final Parcelable.Creator<Country> CREATOR = new Parcelable.Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel source) {
            return new Country(source);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
}
