package com.zuozhi.ideaannotation;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class IdeaAnnotationBundle {
    @NonNls
    private static final String BUNDLE = "messages.IdeaAnnotationBundle";
    private static final DynamicBundle INSTANCE = new DynamicBundle(
        IdeaAnnotationBundle.class,
        BUNDLE
    );

    private IdeaAnnotationBundle() {
    }

    public static @NotNull @Nls String message(
        @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
        Object @NotNull ... params
    ) {
        return INSTANCE.getMessage(key, params);
    }
}
