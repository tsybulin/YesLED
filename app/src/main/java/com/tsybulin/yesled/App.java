package com.tsybulin.yesled;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

public final class App {
    private final String pkgname ;
    private final String name ;
    private final Drawable drawable;
    private boolean checked ;

    public App(String pkgname, String name, Drawable drawable, boolean checked) {
        this.pkgname = pkgname;
        this.name = name;
        this.drawable = drawable ;
        this.checked = checked;
    }

    public String getPkgname() {
        return pkgname;
    }

    public String getName() {
        return name;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
