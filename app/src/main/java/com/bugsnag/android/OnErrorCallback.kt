package com.bugsnag.android

/**
 * A callback to be run before error reports are sent to Bugsnag.
 *
 * You can use this to add or modify information attached to an error
 * before it is sent to your dashboard. You can also return
 * `false` from any callback to halt execution.
 *
 * "on error" callbacks added via the JVM API do not run when a fatal C/C++ crash occurs.
 */
fun interface OnErrorCallback {
    /**
     * Runs the "on error" callback. If the callback returns
     * `false` any further OnErrorCallback callbacks will not be called
     * and the event will not be sent to Bugsnag.
     *
     * @param event the event to be sent to Bugsnag
     * @see Event
     */
    fun onError(event: Event): Boolean
}
