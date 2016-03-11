package barqsoft.footballscores;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;

public class ScoreWidgetIntentService extends IntentService {

    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };
    //these indices must match the projection
    private static final int INDEX_HOME = 0;
    private static final int INDEX_HOME_GOALS = 1;
    private static final int INDEX_AWAY = 2;
    private static final int INDEX_AWAY_GOALS = 3;

    public ScoreWidgetIntentService() {
        super("ScoreWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                ScoreWidgetProvider.class));

        // Get today's date
        String[] date = new String[1];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date[0] = dateFormat.format(System.currentTimeMillis());

        // Get today's data from the ContentProvider
        Uri todaysScores = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor data = getContentResolver().query(todaysScores,
                SCORES_COLUMNS, null, date, null);
        if(data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        boolean gameDay = data.getCount() > 0;

        //Extract the score data from the Cursor
        String homeName = data.getString(INDEX_HOME);
        String homeGoals = data.getString(INDEX_HOME_GOALS);
        int homeGoalsInt = data.getInt(INDEX_HOME_GOALS);

        String awayName = data.getString(INDEX_AWAY);
        String awayGoals = data.getString(INDEX_AWAY_GOALS);
        int awayGoalsInt = data.getInt(INDEX_AWAY_GOALS);
        data.close();

        // Perform this loop procedure for each Score Widget
        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.score_widget);

            //Add the data to the RemoteViews
            if (gameDay) {
                //set team names
                views.setTextViewText(R.id.team1_name, homeName);
                views.setTextViewText(R.id.team2_name, awayName);

                //set scores
                if(homeGoalsInt < 0) {
                    views.setTextViewText(R.id.team1_score, getString(R.string.default_score));
                    views.setTextViewText(R.id.team2_score, getString(R.string.default_score));
                } else {
                    views.setTextViewText(R.id.team1_score, homeGoals);
                    views.setTextViewText(R.id.team2_score, awayGoals);
                }

            } else {
                views.setTextViewText(R.id.team1_name, getString(R.string.no_scores));
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
