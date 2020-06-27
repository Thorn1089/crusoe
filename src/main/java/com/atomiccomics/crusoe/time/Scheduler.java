package com.atomiccomics.crusoe.time;

public interface Scheduler {

    Schedule scheduleRepeatingTask(RepeatingTask task);

}
