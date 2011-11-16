// this class was generated by a tool on Tue Apr 04 14:22:07 EDT 2006

package com.oy.shared.lm.graph.bo;

public class GroupInfo extends com.oy.shared.lm.graph.bo.BaseInfo implements java.io.Serializable {

  public static class Fields {
      public static final String mode = "mode";
  }

  public static class Relations {
  }

  public static class Columns {
      public static final String mode = "";
  }

  private int mode;



  public int getMode (){
    return this.mode;
  }

  public void setMode (int mode){
    this.mode = mode;
  }


  public static class ModeEnum {
      public static final int NONE = 0;
      public static final int OUTLINED = 1;
      public static final int FILLED = 2;
  }

  public boolean isModeNone() {
    return mode == ModeEnum.NONE;
  }
  public void setModeNone() {
    mode = ModeEnum.NONE;
  }
  public boolean isModeOutlined() {
    return mode == ModeEnum.OUTLINED;
  }
  public void setModeOutlined() {
    mode = ModeEnum.OUTLINED;
  }
  public boolean isModeFilled() {
    return mode == ModeEnum.FILLED;
  }
  public void setModeFilled() {
    mode = ModeEnum.FILLED;
  }

  public void copyFieldsFrom(com.oy.shared.lm.graph.bo.GroupInfo source) {
    this.mode = source.mode;
  }
}