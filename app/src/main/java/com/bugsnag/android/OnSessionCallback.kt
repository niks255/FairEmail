package com.bugsnag.android

/**
 * A callback to be run before sessions are sent to Bugsnag.
 *
 * You can use this to add or modify information attached to a session
 * before it is sent to your dashboard. You can also return
 * `false` from any callback to halt execution.
 */
fun interface OnSessionCallback {
    /**
     * Runs the "on session" callback. If the callback returns
     * `false` any further OnSessionCallback callbacks will not be called
     * and the session will not be sent to Bugsnag.
     *
     * @param session the session to be sent to Bugsnag
     * @see Session
     */
    fun onSession(session: Session): Boolean
}
