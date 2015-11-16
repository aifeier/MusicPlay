package com.cwf.app.musicplay;

/**
 * Created by n-240 on 2015/11/16.
 */

        import java.util.ArrayList;
        import java.util.List;

        import android.content.ContentResolver;
        import android.content.ContentUris;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Parcel;
        import android.os.Parcelable;
        import android.provider.MediaStore.Audio.Media;
        import android.util.Log;

public class MusicLoader {

    private static final String TAG = "com.cwf.app.musicplay";

    private static ArrayList<MusicInfo> musicList = new ArrayList<MusicInfo>();

    private static MusicLoader musicLoader;

    private static ContentResolver contentResolver;
    //Uri，指向external的database
    private Uri contentUri = Media.EXTERNAL_CONTENT_URI;
    //projection：选择的列; where：过滤条件; sortOrder：排序。
    private String[] projection = {
            Media._ID,
            Media.DISPLAY_NAME,
            Media.DATA,
            Media.ALBUM,
            Media.ARTIST,
            Media.DURATION,
            Media.SIZE
    };
//    private String where =  "mime_type in ('audio/mpeg','audio/x-ms-wma') and bucket_display_name <> 'audio' and is_music > 0 " ;
    private String sortOrder = Media.SIZE;
    private String where = "duration > 60*1000 and _size > 1024*1024*2";

    public static MusicLoader instance(ContentResolver pContentResolver){
        if(musicLoader == null){
            contentResolver = pContentResolver;
            musicLoader = new MusicLoader();
        }
        return musicLoader;
    }

    private MusicLoader(){
    //利用ContentResolver的query函数来查询数据，然后将得到的结果放到MusicInfo对象中，最后放到数组中
        Cursor cursor = contentResolver.query(contentUri, projection, where, null, sortOrder);
        if(cursor == null){
            Log.v(TAG,"Line(37	)	Music Loader cursor == null.");
        }else if(!cursor.moveToFirst()){
            Log.v(TAG,"Line(39	)	Music Loader cursor.moveToFirst() returns false.");
        }else{
            int displayNameCol = cursor.getColumnIndex(Media.DISPLAY_NAME);
            int albumCol = cursor.getColumnIndex(Media.ALBUM);
            int idCol = cursor.getColumnIndex(Media._ID);
            int durationCol = cursor.getColumnIndex(Media.DURATION);
            int sizeCol = cursor.getColumnIndex(Media.SIZE);
            int artistCol = cursor.getColumnIndex(Media.ARTIST);
            int urlCol = cursor.getColumnIndex(Media.DATA);
            do{
                String title = cursor.getString(displayNameCol);
                String album = cursor.getString(albumCol);
                long id = cursor.getLong(idCol);
                int duration = cursor.getInt(durationCol);
                long size = cursor.getLong(sizeCol);
                String artist = cursor.getString(artistCol);
                String url = cursor.getString(urlCol);

                MusicInfo musicInfo = new MusicInfo(id, title);
                musicInfo.setAlbum(album);
                musicInfo.setDuration(duration);
                musicInfo.setSize(size);
                musicInfo.setArtist(artist);
                musicInfo.setUrl(url);
                musicList.add(musicInfo);

            }while(cursor.moveToNext());
        }
    }

    public ArrayList<MusicInfo> getMusicList(){
        return musicList;
    }

    public Uri getMusicUriById(long id){
        Uri uri = ContentUris.withAppendedId(contentUri, id);
        return uri;
    }
    //下面是自定义的一个MusicInfo子类，实现了Parcelable，为的是可以将整个MusicInfo的ArrayList在Activity和Service中传送，=_=!!,但其实不用
    static class MusicInfo implements Parcelable{
        private long id;
        private String title;
        private String album;
        private int duration;
        private long size;
        private String artist;
        private String url;

        public MusicInfo(){

        }

        public MusicInfo(long pId, String pTitle){
            id = pId;
            title = pTitle;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAlbum() {
            return album;
        }

        public void setAlbum(String album) {
            this.album = album;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeString(title);
            dest.writeString(album);
            dest.writeString(artist);
            dest.writeString(url);
            dest.writeInt(duration);
            dest.writeLong(size);
        }

        public static final Parcelable.Creator<MusicInfo>
                CREATOR = new Creator<MusicLoader.MusicInfo>() {

            @Override
            public MusicInfo[] newArray(int size) {
                return new MusicInfo[size];
            }

            @Override
            public MusicInfo createFromParcel(Parcel source) {
                MusicInfo musicInfo = new MusicInfo();
                musicInfo.setId(source.readLong());
                musicInfo.setTitle(source.readString());
                musicInfo.setAlbum(source.readString());
                musicInfo.setArtist(source.readString());
                musicInfo.setUrl(source.readString());
                musicInfo.setDuration(source.readInt());
                musicInfo.setSize(source.readLong());
                return musicInfo;
            }
        };
    }
}
