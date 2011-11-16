// this class was generated by a tool on Tue Apr 04 14:22:07 EDT 2006

package com.oy.shared.lm.graph.bo;

public class GraphNodeBean implements java.io.Serializable {

  public static class Fields {
      public static final String nodeId = "nodeId";
      public static final String graphId = "graphId";
      public static final String infoId = "infoId";
      public static final String memberOfId = "memberOfId";
  }

  public static class Relations {
      public static final String Graph = "Graph";
      public static final String info = "info";
      public static final String memberOf = "memberOf";
  }

  public static class Columns {
      public static final String nodeId = "";
      public static final String graphId = "";
      public static final String infoId = "";
      public static final String memberOfId = "";
  }

  private int nodeId;
  private int graphId;
  private int infoId;
  private int memberOfId;

  private com.oy.shared.lm.graph.bo.GraphBean Graph;
  private com.oy.shared.lm.graph.bo.NodeInfo info;
  private com.oy.shared.lm.graph.bo.GraphGroupBean memberOf;


  public int getNodeId (){
    return this.nodeId;
  }

  public void setNodeId (int nodeId){
    this.nodeId = nodeId;
  }

  public int getGraphId (){
    return this.graphId;
  }

  public void setGraphId (int graphId){
    this.graphId = graphId;
  }

  public int getInfoId (){
    return this.infoId;
  }

  public void setInfoId (int infoId){
    this.infoId = infoId;
  }

  public int getMemberOfId (){
    return this.memberOfId;
  }

  public void setMemberOfId (int memberOfId){
    this.memberOfId = memberOfId;
  }


  public void setGraph (com.oy.shared.lm.graph.bo.GraphBean Graph){
    this.Graph = Graph;
  }

  public com.oy.shared.lm.graph.bo.GraphBean getGraph (){
    return (com.oy.shared.lm.graph.bo.GraphBean) this.Graph;
  }

  public void setInfo (com.oy.shared.lm.graph.bo.NodeInfo info){
    this.info = info;
  }

  public com.oy.shared.lm.graph.bo.NodeInfo getInfo (){
    return (com.oy.shared.lm.graph.bo.NodeInfo) this.info;
  }

  public void setMemberOf (com.oy.shared.lm.graph.bo.GraphGroupBean memberOf){
    this.memberOf = memberOf;
  }

  public com.oy.shared.lm.graph.bo.GraphGroupBean getMemberOf (){
    return (com.oy.shared.lm.graph.bo.GraphGroupBean) this.memberOf;
  }

  public void copyFieldsFrom(com.oy.shared.lm.graph.bo.GraphNodeBean source) {
    this.nodeId = source.nodeId;
    this.graphId = source.graphId;
    this.infoId = source.infoId;
    this.memberOfId = source.memberOfId;
  }
}