package com.vrockk.custom_view;


import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class CustomEffect {

    private String mEffectName;
    private Map<String, Object> parametersMap;

    private CustomEffect(Builder builder) {
        mEffectName = builder.mEffectName;
        parametersMap = builder.parametersMap;
    }


    public String getEffectName() {
        return mEffectName;
    }


    public Map<String, Object> getParameters() {
        return parametersMap;
    }


    public static class Builder {

        private String mEffectName;
        private Map<String, Object> parametersMap = new HashMap<>();

        /**
         * Initiate your custom effect
         *
         * @param effectName custom effect name from {@link android.media.effect.EffectFactory#createEffect(String)}
         * @throws RuntimeException exception when effect name is empty
         */
        public Builder(@NonNull String effectName) throws RuntimeException {
            if (TextUtils.isEmpty(effectName)) {
                throw new RuntimeException("Effect name cannot be empty.Please provide effect name from EffectFactory");
            }
            mEffectName = effectName;
        }


        public Builder setParameter(@NonNull String paramKey, Object paramValue) {
            parametersMap.put(paramKey, paramValue);
            return this;
        }

        /**
         * @return instance for custom effect
         */
        public CustomEffect build() {
            return new CustomEffect(this);
        }
    }
}
