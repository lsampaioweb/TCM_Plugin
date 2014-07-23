package net.thecodemaster.esvd.nature;

import net.thecodemaster.esvd.constant.Constant;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class Nature implements IProjectNature {

  private IProject project;

  /**
   * {@inheritDoc}
   */
  @Override
  public void configure() throws CoreException {
    // Add the builder to the project.
    IProjectDescription desc = getProject().getDescription();
    ICommand[] commands = desc.getBuildSpec();

    for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(Constant.BUILDER_ID)) {
        return;
      }
    }

    ICommand[] newCommands = new ICommand[commands.length + 1];
    System.arraycopy(commands, 0, newCommands, 0, commands.length);
    ICommand command = desc.newCommand();
    command.setBuilderName(Constant.BUILDER_ID);
    newCommands[newCommands.length - 1] = command;
    desc.setBuildSpec(newCommands);
    getProject().setDescription(desc, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deconfigure() throws CoreException {
    // Remove the builder from the project.
    IProjectDescription description = getProject().getDescription();
    ICommand[] commands = description.getBuildSpec();
    for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(Constant.BUILDER_ID)) {
        ICommand[] newCommands = new ICommand[commands.length - 1];
        System.arraycopy(commands, 0, newCommands, 0, i);
        System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
        description.setBuildSpec(newCommands);
        getProject().setDescription(description, null);
        return;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IProject getProject() {
    return project;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setProject(IProject project) {
    this.project = project;
  }

}
