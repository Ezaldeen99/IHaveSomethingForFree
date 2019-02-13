package gradle.ezaldeen.android.ihavesomethingforfree;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by azozs on 9/12/2018.
 */

public class PostsElement implements Parcelable {

    private String date, description, expireDate, firstName, postImage, profileImage, time, uid;

    public PostsElement(String date, String description, String expireDate, String firstName, String postImage, String profileImage, String time, String title, String uid) {
        this.date = date;
        this.description = description;
        this.expireDate = expireDate;
        this.firstName = firstName;
        this.postImage = postImage;
        this.profileImage = profileImage;
        this.time = time;
        this.uid = uid;
    }

    public PostsElement() {
    }

    private PostsElement(Parcel in) {
        date = in.readString();
        description = in.readString();
        expireDate = in.readString();
        firstName = in.readString();
        postImage = in.readString();
        profileImage = in.readString();
        time = in.readString();
        uid = in.readString();
    }

    public static final Creator<PostsElement> CREATOR = new Creator<PostsElement>() {
        @Override
        public PostsElement createFromParcel(Parcel in) {
            return new PostsElement(in);
        }

        @Override
        public PostsElement[] newArray(int size) {
            return new PostsElement[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(date);
        parcel.writeString(description);
        parcel.writeString(expireDate);
        parcel.writeString(firstName);
        parcel.writeString(postImage);
        parcel.writeString(profileImage);
        parcel.writeString(time);
        parcel.writeString(uid);
    }
}
