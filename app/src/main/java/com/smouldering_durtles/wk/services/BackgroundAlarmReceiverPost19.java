/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smouldering_durtles.wk.services;

import static com.smouldering_durtles.wk.Constants.HOUR;
import static com.smouldering_durtles.wk.util.ObjectSupport.getTopOfHour;
import static com.smouldering_durtles.wk.util.ObjectSupport.runAsync;
import static com.smouldering_durtles.wk.util.ObjectSupport.safe;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.smouldering_durtles.wk.StableIds;
import com.smouldering_durtles.wk.WkApplication;
import com.smouldering_durtles.wk.livedata.LiveAlertContext;
import com.smouldering_durtles.wk.util.Logger;

import javax.annotation.Nullable;

/**
 * The alarm receiver that gets triggered once per hour, and is responsible for
 * notifications, widgets and background sync.
 */
public final class BackgroundAlarmReceiverPost19 extends BroadcastReceiver {
    private static final Logger LOGGER = Logger.get(BackgroundAlarmReceiverPost19.class);

    @Override
    public void onReceive(final Context context, final Intent intent) {
        safe(() -> {
            LOGGER.info("Background alarm post19 received");
            runAsync(() -> LiveAlertContext.getInstance().update());
        });
        safe(BackgroundAlarmReceiver::scheduleOrCancelAlarm);
    }

    /**
     * Schedule the alarm for notifications. It is scheduled for the top
     * of each hour, but depending on circumstances, the delivery of the alarm
     * can be delayed a bit by the device.
     */
    public static void scheduleAlarm() {
        final long nextTrigger = getTopOfHour(System.currentTimeMillis()) + HOUR;
        final @Nullable AlarmManager alarmManager = (AlarmManager) WkApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            final Intent intent = new Intent(WkApplication.getInstance(), BackgroundAlarmReceiverPost19.class);
            final int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            @SuppressLint("UnspecifiedImmutableFlag")
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(WkApplication.getInstance(),
                    StableIds.BACKGROUND_ALARM_REQUEST_CODE_2, intent, flags);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
        }
    }

    /**
     * Cancel the notification alarm.
     */
    public static void cancelAlarm() {
        final @Nullable AlarmManager alarmManager = (AlarmManager) WkApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            final Intent intent = new Intent(WkApplication.getInstance(), BackgroundAlarmReceiverPost19.class);
            final int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            @SuppressLint("UnspecifiedImmutableFlag")
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(WkApplication.getInstance(),
                    StableIds.BACKGROUND_ALARM_REQUEST_CODE_2, intent, flags);
            alarmManager.cancel(pendingIntent);
        }
    }
}
