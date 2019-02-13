package gradle.ezaldeen.android.ihavesomethingforfree.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import gradle.ezaldeen.android.ihavesomethingforfree.MainActivity;
import gradle.ezaldeen.android.ihavesomethingforfree.PostsElement;
import gradle.ezaldeen.android.ihavesomethingforfree.R;

import static gradle.ezaldeen.android.ihavesomethingforfree.MainActivity.postsElements;

/**
 * Created by azozs on 9/24/2018.
 */

public class WidgetProvider extends AppWidgetProvider {
    public static ArrayList<PostsElement> postsElement = new ArrayList<>();

    public WidgetProvider() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
            Log.e("heyy", "create a new one");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (postsElement != null) {
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
                WidgetProvider.
                        updateData(context, appWidgetManager, appWidgetIds, postsElements);
            }

        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    public static void updateData(Context context, AppWidgetManager appWidgetManager, int[] widgetIds, ArrayList<PostsElement> posts) {
        for (int id : widgetIds) {
            updatePostsWidget(context, appWidgetManager, id, posts);
        }
    }

    private static void updatePostsWidget(Context context, AppWidgetManager appWidgetManager, int id, ArrayList<PostsElement> posts) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list);
        Intent intent = new Intent(context, MainActivity.class);
        postsElement = posts;
        PendingIntent pendingIntent = PendingIntent.getActivity(context
                , 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_items_list, pendingIntent);
        Intent i = new Intent(context, WidgetAdapter.class);
        remoteViews.setRemoteAdapter(R.id.widget_items_list, i);
        remoteViews.setEmptyView(R.id.widget_items_list, R.id.empty);
        appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.widget_items_list);
        appWidgetManager.updateAppWidget(id, remoteViews);

    }

}
