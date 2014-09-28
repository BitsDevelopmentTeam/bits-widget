package org.bits.push;

import android.content.Intent;

enum Status {
    DISABLED, OPEN, CLOSED;

    private static final String name = Status.class.getSimpleName();
    public void attachTo(Intent intent) {
        intent.putExtra(name, ordinal());
    }
    public static Status detachFrom(Intent intent) {

        if(!intent.hasExtra(name)) throw new NoSuchFieldError(name);
        return values()[intent.getIntExtra(name, -1)];
    }
}