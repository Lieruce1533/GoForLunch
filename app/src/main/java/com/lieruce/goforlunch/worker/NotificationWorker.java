package com.lieruce.goforlunch.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.UserRepository;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "lunch_notifications";
    private static final int NOTIFICATION_ID = 1;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return Result.success();

        UserRepository userRepository = UserRepository.getInstance();
        
        try {
            // 1. Fetch current user data from Firestore synchronously (blocking the worker thread)
            DocumentSnapshot userDoc = Tasks.await(userRepository.getUserData(firebaseUser.getUid()));
            User user = userDoc.toObject(User.class);

            if (user != null && user.getChosenRestaurantId() != null) {
                // 2. Fetch workmates going to the same restaurant
                QuerySnapshot workmatesDocs = Tasks.await(userRepository.getUsersEatingAt(user.getChosenRestaurantId()).get());
                List<User> workmates = workmatesDocs.toObjects(User.class);
                
                // Remove current user from workmates list manually for API 23 compatibility
                for (int i = 0; i < workmates.size(); i++) {
                    if (workmates.get(i).getUid().equals(user.getUid())) {
                        workmates.remove(i);
                        break;
                    }
                }

                // 3. Prepare and show the notification
                showNotification(user, workmates);
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e("NotificationWorker", "Error fetching data for notification", e);
            return Result.retry();
        }

        return Result.success();
    }

    private void showNotification(User user, List<User> workmates) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Lunch Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        StringBuilder message = new StringBuilder();
        message.append(context.getString(R.string.notification_msg_start, user.getChosenRestaurantName()));
        message.append("\n").append(user.getChosenRestaurantAddress());

        if (!workmates.isEmpty()) {
            message.append("\n").append(context.getString(R.string.notification_msg_workmates));
            for (int i = 0; i < workmates.size(); i++) {
                message.append(workmates.get(i).getUsername());
                if (i < workmates.size() - 1) message.append(", ");
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background) // Replace with a proper icon later
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_msg_short, user.getChosenRestaurantName()))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
