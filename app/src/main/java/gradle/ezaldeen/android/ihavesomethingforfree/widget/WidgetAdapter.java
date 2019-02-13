package gradle.ezaldeen.android.ihavesomethingforfree.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import gradle.ezaldeen.android.ihavesomethingforfree.PostsElement;
import gradle.ezaldeen.android.ihavesomethingforfree.R;

import static gradle.ezaldeen.android.ihavesomethingforfree.widget.WidgetProvider.postsElement;

/**
 * Created by azozs on 9/24/2018.
 */
public class WidgetAdapter extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new remoteViewFactory(this.getApplicationContext(), intent);
    }

    private class remoteViewFactory implements RemoteViewsFactory {
        ArrayList<PostsElement> postsElements = new ArrayList<>();
        Context context;

        remoteViewFactory(Context applicationContext, Intent intent) {
            context = applicationContext;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            PostsElement postsElement1 = postsElements.get(i);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
            remoteViews.setTextViewText(R.id.username_text, postsElement1.getFirstName());
            remoteViews.setTextViewText(R.id.posted_date_text, postsElement1.getDate());
            try {
                Bitmap bitmap = Glide.with(context)
                        .asBitmap()
                        .load(postsElement1.getPostImage())
                        .submit(512, 512)
                        .get();

                remoteViews.setImageViewBitmap(R.id.posts_image, bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            remoteViews.setTextViewText(R.id.describtion_text, postsElement1.getDescription());
            try {
                Bitmap bitmap = Glide.with(context)
                        .asBitmap()
                        .load(postsElement1.getProfileImage())
                        .submit(512, 512)
                        .get();
                remoteViews.setImageViewBitmap(R.id.user_image, bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent fillIntent = new Intent();
            remoteViews.setOnClickFillInIntent(R.id.image_post_layout, fillIntent);
            return remoteViews;
        }

        @Override
        public void onDataSetChanged() {
            postsElements = postsElement;
        }


        @Override
        public int getCount() {
            if (postsElements == null)
                return 0;
            return postsElements.size();
        }


        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public void onCreate() {

        }
    }
}
