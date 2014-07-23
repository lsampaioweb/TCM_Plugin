package net.thecodemaster.esvd.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a timer class, it will calculate how long an operation took from start to finish.
 * 
 * @author Luciano Sampaio 06/03/2014
 * 
 */
public class Timer {
  private String name;
  private long   startTime;
  private long   totalTime;

  public Timer(String name) {
    this.name = name;
  }

  /**
   * Start the timer.
   * 
   * @return An instance of the current time, this method can be chained.
   */
  public Timer start() {
    startTime = getCurrentTime(0);

    return this;
  }

  /**
   * Stop the timer.
   * 
   * @return An instance of the current time, this method can be chained.
   */
  public Timer stop() {
    setTotalTime(getCurrentTime(startTime));

    return this;
  }

  /**
   * @param startTime
   *          The initial time to be subtracted from the final time.
   * @return The current time in milliseconds minus the time passed in as parameter.
   */
  private long getCurrentTime(long startTime) {
    return (System.currentTimeMillis() - startTime);
  }

  /**
   * @return the name of the times.
   */
  public String getName() {
    return name;
  }

  /**
   * @return the totalTime
   */
  public long getTotalTime() {
    return totalTime;
  }

  /**
   * @param totalTime
   *          the totalTime to set
   */
  private void setTotalTime(long totalTime) {
    this.totalTime = totalTime;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS");
    return String.format("%s took %s ms.", getName(), sdf.format(new Date(getTotalTime())));
  }

}
