package net.thecodemaster.esvd.ui.enumeration;

/**
 * @author Luciano Sampaio
 */
public enum EnumVisibilityMenu {
  IS_ENABLED("isEnabled"),
  IS_DISABLED("isDisabled");

  private String name;

  EnumVisibilityMenu(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }

}
