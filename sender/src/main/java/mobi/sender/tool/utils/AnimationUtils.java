package mobi.sender.tool.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Created by Zver on 23.09.2016.
 */
public class AnimationUtils {

    public static void clickAnimation(View v){
        Animation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(2000);
        v.startAnimation(animation);
    }
}
