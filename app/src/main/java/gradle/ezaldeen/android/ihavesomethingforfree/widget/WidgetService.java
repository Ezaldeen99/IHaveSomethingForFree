package gradle.ezaldeen.android.ihavesomethingforfree.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

import gradle.ezaldeen.android.ihavesomethingforfree.PostsElement;

/**
 * Created by azozs on 9/24/2018.
 */

public class WidgetService extends IntentService {
    private static final String POSTS = "POSTS";

    public WidgetService() {
        super("WidgetService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            ArrayList<PostsElement> posts = intent.getParcelableArrayListExtra(POSTS);
            handleUpdateAction(posts);
        }
    }

    private void handleUpdateAction(ArrayList<PostsElement> posts) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
        WidgetProvider.updateData(this, appWidgetManager, widgetIds, posts);

    }

    public static void startService(Context context, ArrayList<PostsElement> postsElements) {
        Log.e("handle update action", context + "");
        Intent i = new Intent(context, WidgetService.class);
        i.putParcelableArrayListExtra(POSTS, postsElements);
        context.startService(i);
    }

}
