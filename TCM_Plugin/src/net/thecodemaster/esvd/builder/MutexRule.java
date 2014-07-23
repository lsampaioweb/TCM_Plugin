package net.thecodemaster.esvd.builder;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * @author Luciano Sampaio
 */
public class MutexRule implements ISchedulingRule {
  @Override
  public boolean isConflicting(ISchedulingRule rule) {
    return rule == this;
  }

  @Override
  public boolean contains(ISchedulingRule rule) {
    return rule == this;
  }
}
