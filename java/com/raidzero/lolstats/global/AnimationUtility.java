package com.raidzero.lolstats.global;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by raidzero on 4/12/15.
 */
public class AnimationUtility {
    private static final String tag = "AnimationUtility";

    public static TranslateAnimation
        getTranslation(int fromX, int toX, int fromY, int toY, int duration,
                       Animation.AnimationListener listener) {

        TranslateAnimation anim = new TranslateAnimation(fromX, toX, fromY, toY);
        anim.setDuration(duration);
        anim.setFillAfter(true);

        anim.setAnimationListener(listener);
        return anim;
    }

    public static AlphaAnimation getAlpha(float from, float to, int duration,
                                          Animation.AnimationListener listener) {
        AlphaAnimation anim = new AlphaAnimation(from, to);

        anim.setDuration(duration);
        anim.setFillAfter(true);

        anim.setAnimationListener(listener);
        return anim;
    }

    public static ScaleAnimation getScale(float fromX, float toX, float fromY, float toY,
                                          int duration, Animation.AnimationListener listener) {
        ScaleAnimation anim = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        anim.setDuration(duration);
        anim.setFillAfter(true);

        anim.setAnimationListener(listener);

        return anim;
    }
}
